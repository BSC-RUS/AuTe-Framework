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

package ru.bsc.test.at.executor.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.ScenarioResult;
import ru.bsc.test.at.executor.model.Stand;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.at.executor.service.api.Executor;
import ru.bsc.test.at.executor.service.api.ProjectExecutorRequest;
import ru.bsc.test.at.executor.service.api.ScenarioExecutorRequest;

import java.util.*;

/**
 * Created by sdoroshin on 21.03.2017.
 */
@Slf4j
@SuppressWarnings("unused")
public class AtProjectExecutor implements Executor<ProjectExecutorRequest> {

    private static final int POLLING_RETRY_COUNT = 50;
    private static final int POLLING_RETRY_TIMEOUT_MS = 1000;
    private static final String DEFAULT_CONTENT_TYPE = "text/xml";

    private String projectPath;

    public AtProjectExecutor(String projectPath) {
        this.projectPath = projectPath;
    }

    @Override
    public void execute(ProjectExecutorRequest projectExecutorRequest) {
        Assert.notNull(projectExecutorRequest, "projectExecutorRequest must not be null");
        // Создать подключение к БД, которое будет использоваться сценарием для select-запросов.
        Set<Stand> standSet = new LinkedHashSet<>();
        // Собрать список всех используемых стендов выбранными сценариями
        if (projectExecutorRequest.getProject().getStand() != null) {
            standSet.add(projectExecutorRequest.getProject().getStand());
        }
        AtScenarioExecutor atScenarioExecutor = new AtScenarioExecutor();
        List<ScenarioResult> scenarioResultList = projectExecutorRequest.getScenarioResultList();
        try (ExecutorJdbcConnectionHolder executorJdbcConnectionHolder = new ExecutorJdbcConnectionHolder(standSet)) {
            for (Scenario scenario : projectExecutorRequest.getScenarioExecuteList()) {
                List<StepResult> stepResultList = new LinkedList<>();
                scenarioResultList.add(new ScenarioResult(scenario, stepResultList));
                atScenarioExecutor.execute(
                        new ScenarioExecutorRequest(projectExecutorRequest.getProject(), scenario, projectExecutorRequest.getProject().getStand(),
                                executorJdbcConnectionHolder.getConnection(projectExecutorRequest.getProject().getStand()),
                                stepResultList, projectPath, projectExecutorRequest.getStopObserver())
                );
            }
        }
        projectExecutorRequest.getFinishObserver().finish(scenarioResultList);
    }


    public static long parseLongOrVariable(Map<String, Object> scenarioVariables, String value, long defaultValue) {
        log.debug("parseLongOrVariable {}, {}, {}", scenarioVariables, value, defaultValue);
        long result;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            try {
                result = Integer.parseInt(String.valueOf(scenarioVariables.get(value)));
            } catch (NumberFormatException ex) {
                log.info("parseLongOrVariable: got error! Take the default value: {}", ex.getMessage());
                result = defaultValue;
            }
        }
        log.debug("parseLongOrVariable result {}", result);
        return result;
    }
}
