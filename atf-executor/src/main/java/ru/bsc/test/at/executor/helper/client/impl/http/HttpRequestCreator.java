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

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.*;

import java.net.URI;

/**
 * @author Pavel Golovkin
 */
class HttpRequestCreator {

  static HttpRequestBase createRequest(HTTPMethod method, URI uri, String testIdHeaderName, String testId) {
    HttpRequestBase httpRequest;
    switch (method == null ? HTTPMethod.POST : method) {
      case GET:
        httpRequest = new HttpGet(uri);
        break;
      case DELETE:
        httpRequest = new HttpDelete(uri);
        break;
      case PUT:
        httpRequest = new HttpPut(uri);
        break;
      case PATCH:
        httpRequest = new HttpPatch(uri);
        break;
      case POST:
      default:
        httpRequest = new HttpPost(uri);
        break;
    }
    if (StringUtils.isNotEmpty(testIdHeaderName)) {
      httpRequest.addHeader(testIdHeaderName, testId);
    }
    return httpRequest;
  }
}
