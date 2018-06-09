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

package ru.bsc.test.at.executor.helper.client.api;

import lombok.AllArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * @author Pavel Golovkin
 */
@AllArgsConstructor
public class ClientCommonResponse implements ClientResponse {
  private final int statusCode;
  private final String content;
  private final Map<String, List<String>> headers;

  @Override
  public int getStatusCode() {
    return statusCode;
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return headers;
  }
}
