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

package ru.bsc.test.autotester.report;

import lombok.Getter;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.StepResult;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
public abstract class AbstractReportGenerator {

    private final Map<Scenario, List<StepResult>> scenarioStepResultMap = new LinkedHashMap<>();

    public void add(Scenario scenario, List<StepResult> stepResultList) {
        scenarioStepResultMap.put(scenario, stepResultList);
    }

    public void clear() {
        scenarioStepResultMap.clear();
    }

    public boolean isEmpty() {
        return scenarioStepResultMap.isEmpty();
    }

    public abstract void generate(File directory) throws Exception;
}
