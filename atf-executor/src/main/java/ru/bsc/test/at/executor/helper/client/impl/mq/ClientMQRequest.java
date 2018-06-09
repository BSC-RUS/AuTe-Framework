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

package ru.bsc.test.at.executor.helper.client.impl.mq;

import lombok.AllArgsConstructor;
import ru.bsc.test.at.executor.helper.client.api.ClientRequest;

import java.util.Map;

/**
 * @author Pavel Golovkin
 */
@AllArgsConstructor
public class ClientMQRequest implements ClientRequest {
  private final String queueName;
  private final String messageBody;
  private final Map headers;
  private final String testId;
  private final String testIdHeaderName;

  @Override
  public String getResource() {
    return queueName;
  }

  @Override
  public Object getBody() {
    return messageBody;
  }

  @Override
  public Map getHeaders() {
    return headers;
  }

  @Override
  public String getTestId() {
    return testId;
  }

  @Override
  public String getTestIdHeaderName() {
    return testIdHeaderName;
  }
}
