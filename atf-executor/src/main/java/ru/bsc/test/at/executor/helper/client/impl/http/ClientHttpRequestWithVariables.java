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

import ru.bsc.test.at.executor.model.Step;

import java.util.Map;

/**
 * @author Pavel Golovkin
 */
public final class ClientHttpRequestWithVariables extends ClientHttpRequest {
  private final Map<String, Object> scenarioVariables;
  private final String projectPath;
  private final Step step;

  public ClientHttpRequestWithVariables(String url, Object body, HTTPMethod method, Map headers, String testId, String testIdName, Map<String, Object> scenarioVariables, String projectPath, Step step) {
    super(url, body, method, headers, testId, testIdName);
    this.scenarioVariables = scenarioVariables;
    this.projectPath = projectPath;
    this.step = step;
  }

  public Map<String, Object> getScenarioVariables() {
    return scenarioVariables;
  }

  public String getProjectPath() {
    return projectPath;
  }

  public Step getStep() {
    return step;
  }
}
