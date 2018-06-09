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

package ru.bsc.test.autotester.repository;

import ru.bsc.test.at.executor.model.Scenario;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * Created by sdoroshin on 27.10.2017.
 *
 */
public interface ScenarioRepository {
    List<Scenario> findScenarios(String projectCode);

    List<Scenario> findScenariosWithSteps(String projectCode);

    Scenario findScenario(String projectCode, String scenarioPath) throws IOException;

    Scenario saveScenario(String projectCode, String scenarioPath, Scenario data, boolean updateDirectoryName) throws IOException;

    Set<Scenario> findByRelativeUrl(String projectCode, String relativeUrl);

    void delete(String projectCode, String scenarioPath) throws IOException;
}
