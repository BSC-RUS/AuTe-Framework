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
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.util.Assert;
import ru.bsc.test.at.executor.exception.ScenarioStopException;
import ru.bsc.test.at.executor.helper.client.impl.http.HttpClient;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.service.api.Executor;
import ru.bsc.test.at.executor.service.api.ScenarioExecutorRequest;
import ru.bsc.test.at.executor.service.api.StepExecutorRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Pavel Golovkin
 */
@Slf4j
public class AtScenarioExecutor implements Executor<ScenarioExecutorRequest> {

    @Override
    public void execute(ScenarioExecutorRequest scenarioExecutorRequest) {
        Assert.notNull(scenarioExecutorRequest, "scenarioExecutorRequest must not be null");
        Map<String, Object> scenarioVariables = new HashMap<>();
        scenarioVariables.put("__random", RandomStringUtils.randomAlphabetic(40));

        AtStepExecutor atStepExecutor = new AtStepExecutor();

        Project project = scenarioExecutorRequest.getProject();
        try (
                HttpClient httpClient = new HttpClient();
                MqClient mqClient = project.getAmqpBroker() != null ? new MqClient(project.getAmqpBroker()) : null
        ) {
            // перед выполнением каждого сценария выполнять предварительный сценарий, заданный в свойствах проекта (например, сценарий авторизации)
            Scenario beforeScenario = scenarioExecutorRequest.getScenario().getBeforeScenarioIgnore() ? null : findScenarioByPath(project.getBeforeScenarioPath(), project.getScenarioList());
            if (beforeScenario != null) {
                atStepExecutor.execute(
                        new StepExecutorRequest(scenarioExecutorRequest.getConnection(), scenarioExecutorRequest.getStand(), beforeScenario.getStepList(),
                                project, scenarioExecutorRequest.getStepResultList(), httpClient, mqClient, scenarioVariables, false,
                                scenarioExecutorRequest.getProjectPath(), scenarioExecutorRequest.getStopObserver())
                );
            }

            atStepExecutor.execute(
                    new StepExecutorRequest(scenarioExecutorRequest.getConnection(), scenarioExecutorRequest.getStand(),
                            scenarioExecutorRequest.getScenario().getStepList(), project, scenarioExecutorRequest.getStepResultList(),
                            httpClient, mqClient, scenarioVariables, true, scenarioExecutorRequest.getProjectPath(),
                            scenarioExecutorRequest.getStopObserver())
            );

            // После выполнения сценария выполнить сценарий, заданный в проекте или в сценарии
            Scenario afterScenario = scenarioExecutorRequest.getScenario().getAfterScenarioIgnore() ? null : findScenarioByPath(project.getAfterScenarioPath(), project.getScenarioList());
            if (afterScenario != null) {
                atStepExecutor.execute(new StepExecutorRequest(scenarioExecutorRequest.getConnection(), scenarioExecutorRequest.getStand(),
                        afterScenario.getStepList(), project, scenarioExecutorRequest.getStepResultList(), httpClient,
                        mqClient, scenarioVariables, false, scenarioExecutorRequest.getProjectPath(),
                        scenarioExecutorRequest.getStopObserver())
                );
            }

        } catch (ScenarioStopException | InterruptedException e) {
            // Stop scenario executing
            log.error("Error during scenario execution", e);
        } catch (Exception e) {
            log.error("Error during MqClient get connection", e);
        }

    }

    private Scenario findScenarioByPath(String path, List<Scenario> scenarioList) {
        log.debug("findScenarioByPath {}", path);
        if (path == null) {
            log.warn("findScenarioByPath: path is null, return null");
            return null;
        }
        String scenarioCode;
        String scenarioGroupCode;
        String[] scenarioPathParts = path.split("/");
        if (scenarioPathParts.length == 1) {
            scenarioGroupCode = null;
            scenarioCode = scenarioPathParts[0];
        } else if (scenarioPathParts.length == 2) {
            scenarioGroupCode = scenarioPathParts[0];
            scenarioCode = scenarioPathParts[1];
        } else {
            log.warn("findScenarioByPath: NOT FOUND!");
            return null;
        }

        Scenario result = scenarioList.stream()
                .filter(scenario -> Objects.equals(scenario.getCode(), scenarioCode))
                .filter(scenario -> scenarioGroupCode == null || Objects.equals(scenarioGroupCode, scenario.getScenarioGroup()))
                .findAny()
                .orElse(null);
        log.debug("findScenarioByPath result {}", result);
        return result;
    }

}
