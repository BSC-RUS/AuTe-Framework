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

package ru.bsc.test.at.mock.mq.http;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import ru.bsc.test.at.client.impl.http.HTTPClientBuilder;

import java.io.Closeable;
import java.io.IOException;

@Slf4j
public class HttpClient implements Closeable {

    private CloseableHttpClient closeableHttpClient;

    public HttpClient() {
        closeableHttpClient = new HTTPClientBuilder().withGlobalConfig().withCookiesStore().build();
    }

    public String sendPost(String url, String body, String headerName, String headerValue) throws IOException {
        HttpPost post = new HttpPost(url);
        post.addHeader(headerName, headerValue);
        post.setEntity(new StringEntity(body));
        try (CloseableHttpResponse response = closeableHttpClient.execute(post)) {
            return extractResult(response);
        }
    }

    private String extractResult(CloseableHttpResponse response) throws IOException {
        return response.getEntity() == null || response.getEntity().getContent() == null ? "" : IOUtils.toString(
                response.getEntity().getContent(),
                "UTF-8"
        );
    }

    @Override
    public void close() throws IOException {
        try {
            closeableHttpClient.close();
        } catch (Exception e) {
            log.error("Error closing http connection", e);
            throw new IOException(e);
        }
    }
}