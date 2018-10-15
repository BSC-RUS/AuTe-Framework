/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the AuTe Framework project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.at.executor.helper.client.impl.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.tika.Tika;
import ru.bsc.test.at.client.impl.http.HTTPClientBuilder;
import ru.bsc.test.at.executor.helper.client.api.Client;
import ru.bsc.test.at.executor.helper.client.api.ClientCommonResponse;
import ru.bsc.test.at.executor.model.FieldType;
import ru.bsc.test.at.executor.model.FormData;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.step.executor.AbstractStepExecutor;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sdoroshin on 22/05/17.
 *
 */
@Slf4j
public class HttpClient implements Client<ClientHttpRequest, ClientCommonResponse> {
    private final CloseableHttpClient httpClient;
    private final HttpClientContext context;

    public HttpClient() {
        context = HttpClientContext.create();
        httpClient = new HTTPClientBuilder().withSllContext().withGlobalConfig().withCookiesStore().build();
    }

    public List<Cookie> getCookies() {
        return context.getCookieStore().getCookies();
    }

    @Override
    public ClientCommonResponse request(ClientHttpRequest request) throws Exception {
        if (request instanceof ClientHttpRequestWithVariables) {
            return executeWithScenarioVariables((ClientHttpRequestWithVariables) request);
        } else if (request instanceof ClientHttpRequest) {
            return executeWithoutScenarioVariables(request);
        } else {
            throw new Exception("Unsupported request " + request.getClass());
        }
    }

    private ClientCommonResponse executeWithoutScenarioVariables(ClientHttpRequest request) throws Exception {
        URI uri = new URIBuilder(request.getResource()).build();
        HttpRequestBase httpRequest = HttpRequestCreator.createRequest(request.getMethod(), uri, request.getTestIdHeaderName(), request.getTestId());
        if (httpRequest instanceof HttpEntityEnclosingRequestBase && request.getBody() != null) {
            HttpEntity httpEntity = new StringEntity((String) request.getBody(), ContentType.APPLICATION_JSON);
            ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(httpEntity);
        }
        return execute(httpRequest, request.getHeaders());
    }

    private ClientCommonResponse executeWithScenarioVariables(ClientHttpRequestWithVariables request) throws Exception {
        URI uri = new URIBuilder(request.getResource()).build();
        HttpRequestBase httpRequest = HttpRequestCreator.createRequest(request.getMethod(), uri, request.getTestIdHeaderName(), request.getTestId());
        if (httpRequest instanceof HttpEntityEnclosingRequestBase) {
            boolean useMultipartFormData;
            Step step = request.getStep();
            if (step.getMultipartFormData() == null) {
                long count = step.getFormDataList().stream().filter(formData1 -> FieldType.FILE.equals(formData1.getFieldType())).count();
                useMultipartFormData = count > 0;
            } else {
                useMultipartFormData = step.getMultipartFormData();
            }
            HttpEntity httpEntity;
            if (useMultipartFormData) {
                httpEntity = setEntity(step.getFormDataList(), request.getProjectPath(), request.getScenarioVariables()).build();
            } else {
                List<NameValuePair> params = step.getFormDataList()
                        .stream()
                        .map(formData1 -> new BasicNameValuePair(formData1.getFieldName(), AbstractStepExecutor.insertSavedValues(formData1.getValue(), request.getScenarioVariables())))
                        .collect(Collectors.toList());
                httpEntity = new UrlEncodedFormEntity(params);
            }
            ((HttpEntityEnclosingRequestBase) httpRequest).setEntity(httpEntity);
        }
        return execute(httpRequest, request.getHeaders());
    }

    private MultipartEntityBuilder setEntity(List<FormData> formDataList, String projectPath, Map<String, Object> scenarioVariables) throws IOException {
        MultipartEntityBuilder entity = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        for (FormData formData : formDataList) {
            if (formData.getFieldType() == null || FieldType.TEXT.equals(formData.getFieldType())) {
                entity.addTextBody(formData.getFieldName(), AbstractStepExecutor.insertSavedValues(formData.getValue(), scenarioVariables), ContentType.TEXT_PLAIN);
            } else {
                log.debug("Try to identify Mime type projectPath = {}, formData = {}, fromData.getFilePath = {}", projectPath, formData, formData.getFilePath());
                File file = new File((projectPath == null ? "" : projectPath) + formData.getFilePath());
                String detectedMimeType = new Tika().detect(file);
                log.debug("Tika detection result = {}", detectedMimeType);
                log.debug("Try to get content type from formData.getMimeType = {}, tika detected mime type = {}", formData.getMimeType(), detectedMimeType);
                entity.addBinaryBody(
                        formData.getFieldName(),
                        file,
                        ContentType.parse( StringUtils.isEmpty(formData.getMimeType()) ? detectedMimeType : formData.getMimeType()),
                        file.getName()
                );
            }
        }
        return entity;
    }

    private ClientCommonResponse execute(HttpRequestBase httpRequest, Map headers) throws IOException {
        setHeaders(httpRequest, headers);
        try (CloseableHttpResponse response = httpClient.execute(httpRequest, context)) {
            Map<String, List<String>> responseHeaders = new HashMap<>();
            Arrays.stream(response.getAllHeaders()).forEach(header -> {
                List<String> values = responseHeaders.get(header.getName());
                if (values != null) {
                    values.add(header.getValue());
                } else {
                    List<String> list = new LinkedList<>();
                    list.add(header.getValue());
                    responseHeaders.put(header.getName(), list);
                }
            });
            String theString = response.getEntity() == null || response.getEntity().getContent() == null ? "" : IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return new ClientCommonResponse(response.getStatusLine().getStatusCode(), theString, responseHeaders);
        }
    }

    private void setHeaders(HttpRequestBase request, Map headers) {
        if (headers == null || headers.isEmpty()) {
            return;
        }
        for (Object key: headers.keySet()) {
            if (key != null) {
                String keyStr = (String) key;
                if (StringUtils.isNotEmpty(keyStr)) {
                    request.addHeader(keyStr, (String) headers.get(key));
                }
            }
        }
    }

    public void close() throws IOException {
        try {
            httpClient.close();
        } catch (IOException e) {
            log.error("Error while closing http connection", e);
            throw e;
        }
    }
}
