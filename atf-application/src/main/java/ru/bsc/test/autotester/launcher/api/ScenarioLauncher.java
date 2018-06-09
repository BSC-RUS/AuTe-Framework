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

package ru.bsc.test.autotester.launcher.api;

import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.ScenarioResult;
import ru.bsc.test.autotester.model.ExecutionResult;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.report.AbstractReportGenerator;
import ru.bsc.test.autotester.service.ProjectService;

import java.util.List;
import java.util.Set;

/**
 * Launch scenario interface.
 *
 * @author Pavel Golovkin
 */
public interface ScenarioLauncher {
  /**
   * Launch scenario from UI
   * @param scenarioList
   * @param project
   * @param properties
   * @param executionResult
   * @param stopExecutingSet
   * @param projectService
   * @param runningUuid
   */
  void launchScenarioFromUI(List<Scenario> scenarioList,
                            Project project,
                            EnvironmentProperties properties,
                            ExecutionResult executionResult,
                            Set<String> stopExecutingSet,
                            ProjectService projectService,
                            String runningUuid);

  /**
   * Launch scenario from command line
   * @param scenarioToExecute
   * @param project
   * @param properties
   * @param reportGenerator
   * @return test launch result
   */
  List<ScenarioResult> launchScenarioFromCLI(List<Scenario> scenarioToExecute,
                                     Project project,
                                     EnvironmentProperties properties,
                                     AbstractReportGenerator reportGenerator);
}
