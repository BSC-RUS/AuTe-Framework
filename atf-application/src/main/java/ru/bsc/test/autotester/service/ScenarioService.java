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

package ru.bsc.test.autotester.service;

import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.autotester.model.ExecutionResult;
import ru.bsc.test.autotester.ro.*;

import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;

/**
 * Created by sdoroshin on 03.03.2017.
 *
 */
public interface ScenarioService {

    Scenario findOne(String projectCode, String scenarioPath) throws IOException;

    ScenarioRo updateScenarioFormRo(String projectCode, String scenarioPath, ScenarioRo scenarioRo) throws IOException;

    Step cloneStep(String projectCode, String scenarioPath, String stepCode) throws IOException;

    List<StepRo> updateStepListFromRo(String projectCode, String scenarioPath, List<StepRo> stepRoList) throws IOException;

    StartScenarioInfoRo startScenarioExecutingList(Project project, List<Scenario> scenarioList);

    void stopExecuting(String executingUuid);

    List<String> getExecutingList();

    ExecutionResult getResult(String executingUuid);

    List<StepResult> getResult(ScenarioIdentityRo identity);

    StepRo addStepToScenario(String projectCode, String scenarioPath, StepRo stepRo) throws IOException;

    Scenario saveScenario(String projectCode, String scenarioPath, Scenario scenario) throws IOException;

    void deleteOne(String projectCode, String scenarioPath) throws IOException;

    List<ScenarioRo> findScenarioByStepRelativeUrl(String projectCode, ProjectSearchRo projectSearchRo);

    StepRo updateStepFromRo(String projectCode, String scenarioPath, String stepCode, StepRo stepRo) throws IOException;

    List<Scenario> findAllByProject(String projectCode);

    ScenarioRo addScenarioToProject(String projectCode, ScenarioRo scenarioRo) throws IOException;

    void getReport(List<ScenarioIdentityRo> identities, ZipOutputStream executionUuid) throws Exception;
}
