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

/**
 * @author Pavel Golovkin
 */
interface WireMockUrl {

  String DELETE_REST_MAPPING_URL = "/__admin/mappings/";
  String ADD_REST_MAPPING_URL = "/__admin/mappings/";
  String FIND_REST_REQUEST_URL = "/__admin/requests/find";
  String FIND_MQ_REQUEST_LIST_URL = "/mq-mock/__admin/request-list";
  String ADD_MQ_MAPPING_URL = "/mq-mock/__admin/add-mapping";
}
