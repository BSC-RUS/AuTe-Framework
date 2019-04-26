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

package ru.bsc.test.at.executor.ei.wiremock;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import ru.bsc.test.at.executor.ei.wiremock.model.*;

import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by sdoroshin on 27.07.2017.
 */
@Slf4j
public class WireMockAdmin implements Closeable {

    private final WireMockUrlBuilder wireMockUrlBuilder;
    private final List<String> mockIdList = new LinkedList<>();
    private final List<String> mockGuidList = new LinkedList<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public WireMockAdmin(String wireMockAdminUrl) {
        this.wireMockUrlBuilder = new WireMockUrlBuilder(wireMockAdminUrl);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private void clearRestSavedMappings() {
        mockIdList.forEach(s -> sendDelete(wireMockUrlBuilder.deleteRestMappingUrl() + s));
    }

    public void addRestMapping(MockDefinition mockDefinition) throws IOException {
        String result = sendPost(wireMockUrlBuilder.addRestMappingUrl(), mapper.writeValueAsString(mockDefinition));
        MockDefinition mockDefinitionResponse = mapper.readValue(result, MockDefinition.class);
        mockIdList.add(mockDefinitionResponse.getUuid());
    }

    public List<WireMockRequest> findRestRequests(MockRequest mockRequest) throws IOException {
        String result = sendPost(wireMockUrlBuilder.findRestRequestListUrl(), mapper.writeValueAsString(mockRequest));
        try {
            return mapper.readValue(result, RequestList.class).getRequests();
        } catch (IOException e) {
            log.error("error while getting requests to mocks", e);
            return Collections.emptyList();
        }
    }

    public long countRestRequests(MockRequest request) throws IOException {
        String response = sendPost(wireMockUrlBuilder.countRequests(), mapper.writeValueAsString(request));
        return mapper.readValue(response, RequestsCount.class).getCount();
    }

    private String sendPost(String url, String jsonRequestBody) throws IOException {
        log.debug("Post request. Url = {}, body = {}", url, jsonRequestBody);
        HttpPost httpPostAddMapping = new HttpPost(url);
        httpPostAddMapping.setEntity(new StringEntity(jsonRequestBody, ContentType.APPLICATION_JSON));

        try (CloseableHttpResponse response = HttpClientBuilder.create().build().execute(httpPostAddMapping)) {
            String responseBody = response.getEntity() == null || response.getEntity().getContent() == null ? "" : IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            log.debug("Post response = {}", responseBody);
            return responseBody;
        }
    }

    private String sendDelete(String url) {
        try (CloseableHttpResponse response = HttpClientBuilder.create().build().execute(new HttpDelete(url))) {
            return response.getEntity() == null || response.getEntity().getContent() == null ? "" : IOUtils.toString(response.getEntity().getContent(), "UTF-8");
        } catch (IOException e) {
            log.error("Error deleting mock", e);
            throw new UncheckedIOException(e);
        }
    }

    @Override
    public void close() {
        clearMqMockList();
        clearRestSavedMappings();
    }

    private List<MockedRequest> getMqRequestList() throws IOException {
        try (CloseableHttpResponse response = HttpClientBuilder.create().build().execute(new HttpGet(wireMockUrlBuilder.findMQRequestListUrl()))) {
            String responseBody = response.getEntity() == null || response.getEntity().getContent() == null ? "" : IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            return mapper.readValue(responseBody, new TypeReference<List<MockedRequest>>(){});
        }
    }

    public List<MockedRequest> getMqRequestListByTestId(String testId) throws IOException {
        return getMqRequestList()
                .stream()
                .filter(mockedRequest -> Objects.equals(mockedRequest.getTestId(), testId))
                .collect(Collectors.toList());
    }

    public List<MockedRequest>  getMqRequestListByTestIdAndQueue(String sourceQueue, String testId) throws IOException {
        return getMqRequestList()
            .stream()
            .filter(mockedRequest -> Objects.equals(mockedRequest.getTestId(), testId) && Objects.equals(mockedRequest.getSourceQueue(), sourceQueue))
            .collect(Collectors.toList());
    }

    public void addMqMapping(MqMockDefinition mockMessageDefinition) throws IOException {
        HttpPost httpPostAddMapping = new HttpPost(wireMockUrlBuilder.addMQMappingUrl());
        httpPostAddMapping.setEntity(new StringEntity(mapper.writeValueAsString(mockMessageDefinition), ContentType.APPLICATION_JSON));
        try (CloseableHttpResponse response = HttpClientBuilder.create().build().execute(httpPostAddMapping)) {
            String mockGuid = response.getEntity() == null || response.getEntity().getContent() == null ? "" : IOUtils.toString(response.getEntity().getContent(), "UTF-8");
            mockGuidList.add(mockGuid);
        }
    }

    private void clearMqMockList() {
        // TODO нельзя удалять заглушки очередей сразу после завершения шага, так как MQ-Mocker может не успеть получить нужное сообщение и обработать его
        // mockGuidList.forEach(guid -> sendDelete("/mappings/" + guid));
    }
}
