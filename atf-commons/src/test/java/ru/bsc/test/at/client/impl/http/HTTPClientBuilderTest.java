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

package ru.bsc.test.at.client.impl.http;

import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

/**
 * @author Pavel Golovkin
 */

public class HTTPClientBuilderTest {

    @Test
    public void testEmptyBuild() {
        CloseableHttpClient closeableHttpClient = new HTTPClientBuilder().build();
        assertNotNull(closeableHttpClient);
    }

    @Test
    public void testWithCookiesStoreBuild() {
        CloseableHttpClient closeableHttpClient = new HTTPClientBuilder().withCookiesStore().build();
        assertNotNull(closeableHttpClient);
    }

    @Test
    public void testWithGlobalConfigBuild() {
        CloseableHttpClient closeableHttpClient = new HTTPClientBuilder().withGlobalConfig().build();
        assertNotNull(closeableHttpClient);
    }

    @Test
    public void testWithSllContextBuild() {
        CloseableHttpClient closeableHttpClient = new HTTPClientBuilder().withSllContext().build();
        assertNotNull(closeableHttpClient);
    }

    @Test
    public void testWithAllBuild() {
        CloseableHttpClient closeableHttpClient = new HTTPClientBuilder().withCookiesStore()
                .withGlobalConfig()
                .withSllContext()
                .build();
        assertNotNull(closeableHttpClient);
    }
}