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

import org.springframework.util.Assert;

import static ru.bsc.test.at.executor.ei.wiremock.WireMockUrl.*;

/**
 * @author Pavel Golovkin
 */
public class WireMockUrlBuilder {

  private String baseUrl;

  WireMockUrlBuilder(String baseUrl) {
    Assert.notNull(baseUrl, "WireMock base url must not be null");
    this.baseUrl = baseUrl;
  }

  String deleteRestMappingUrl() {
    return baseUrl + DELETE_REST_MAPPING_URL;
  }

  String addRestMappingUrl() {
    return baseUrl + ADD_REST_MAPPING_URL;
  }

  String findRestRequestListUrl() {
    return baseUrl + FIND_REST_REQUEST_URL;
  }

  String findMQRequestListUrl() {
    return baseUrl + FIND_MQ_REQUEST_LIST_URL;
  }

  String addMQMappingUrl() {
    return baseUrl + ADD_MQ_MAPPING_URL;
  }
}
