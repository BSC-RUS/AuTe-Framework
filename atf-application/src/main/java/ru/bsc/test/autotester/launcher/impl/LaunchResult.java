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

package ru.bsc.test.autotester.launcher.impl;

import lombok.ToString;
import ru.bsc.test.at.executor.model.ScenarioResult;

import java.util.List;

/**
 * @author Pavel Golovkin
 */
@ToString
public class LaunchResult {

  private int failedTestsCount;
  private int passedTestsCount;

  public boolean isFailed() {
    return failedTestsCount > 0;
  }

  LaunchResult(List<ScenarioResult> scenarioResults) {
    for (ScenarioResult scenarioResult: scenarioResults) {
      if (scenarioResult.isFailed()) {
        failedTestsCount++;
      } else {
        passedTestsCount++;
      }
    }
  }
}
