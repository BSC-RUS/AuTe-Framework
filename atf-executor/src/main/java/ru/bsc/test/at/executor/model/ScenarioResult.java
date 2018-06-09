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

package ru.bsc.test.at.executor.model;

import lombok.ToString;

import java.util.List;

/**
 * @author Pavel Golovkin
 */
@ToString
public class ScenarioResult {
  private Scenario scenario;
  private List<StepResult> stepResultList;
  private Boolean failed;

  public ScenarioResult(Scenario scenario, List<StepResult> stepResultList) {
    this.scenario = scenario;
    this.stepResultList = stepResultList;
  }

  public List<StepResult> getStepResultList() {
    return stepResultList;
  }

  public Scenario getScenario() {
    return scenario;
  }

  public boolean isFailed() {
    if (failed == null) {
      failed = false;
      for (StepResult stepResult: stepResultList) {
        if (!stepResult.getResult().isPositive()) {
          failed = true;
        }
      }
    }
    return failed;
  }

}
