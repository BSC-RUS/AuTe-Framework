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

package ru.bsc.test.at.executor.step.executor;

import lombok.extern.slf4j.Slf4j;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.helper.client.impl.http.HttpClient;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.at.executor.step.executor.requester.MqPollingStepRequester;
import ru.bsc.test.at.executor.step.executor.requester.MqSimpleStepRequester;
import ru.bsc.test.at.executor.step.executor.requester.StepRequester;

import java.sql.Connection;
import java.util.Map;

@Slf4j
public class MqStepExecutor implements IStepExecutor {

    @Override
    public void execute(WireMockAdmin wireMockAdmin, Connection connection, Stand stand, HttpClient httpClient, MqClient mqClient, Map<String, Object> scenarioVariables, String testId, Project project, Scenario scenario, Step step, StepResult stepResult, String projectPath) throws Exception {

        if (mqClient == null) {
            throw new Exception("JMS is not configured in env.yml");
        }

        // 0. Установить ответы сервисов, которые будут использоваться в SoapUI для определения ответа
        ExecutorUtils.setMockResponses(wireMockAdmin, project, testId, step.getMockServiceResponseList(), step.getCode(), scenario.getName(), scenarioVariables);

        // 0.1 Установить ответы для имитации внешних сервисов, работающих через очереди сообщений
        ExecutorUtils.setMqMockResponses(wireMockAdmin, testId, step.getMqMockResponseList(), scenarioVariables);

        // 1. Выполнить запрос БД и сохранить полученные значения
        ExecutorUtils.executeSql(connection, step, scenarioVariables, stepResult);
        stepResult.setSavedParameters(scenarioVariables.toString());

        // 1.1 Отправить сообщение в очередь
        ExecutorUtils.sendMessagesToQuery(project, step, scenarioVariables, mqClient, project.getTestIdHeaderName(), testId);

        // 2. Подстановка сохраненных параметров в строку запроса
        String requestUrl = stand.getServiceUrl() + ExecutorUtils.insertSavedValues(step.getRelativeUrl(), scenarioVariables);
        stepResult.setRequestUrl(requestUrl);

        // 2.1 Подстановка сохраненных параметров в тело запроса
        String requestBody = ExecutorUtils.insertSavedValues(step.getRequest(), scenarioVariables);

        // 2.2 Вычислить функции в теле запроса
        requestBody = ExecutorUtils.evaluateExpressions(requestBody, scenarioVariables);
        stepResult.setRequestBody(requestBody);

        // 2.4 Cyclic sending request, COM-84
        int numberRepetitions = ExecutorUtils.calculateNumberRepetitions(step, scenarioVariables);

        for (int repetitionCounter = 0; repetitionCounter < numberRepetitions; repetitionCounter++) {
            StepRequester stepRequester;
            if (step.getUsePolling()) {
                stepRequester = new MqPollingStepRequester(stepResult, step, requestBody, testId,  project, mqClient, scenarioVariables, projectPath);
            } else {
                stepRequester = new MqSimpleStepRequester(stepResult, step, requestBody, testId,  project, mqClient, scenarioVariables, projectPath);
            }
            stepRequester.request();
        }
    }

    @Override
    public boolean support(Step step) {
        return StepMode.JMS.equals(step.getStepMode());
    }
}
