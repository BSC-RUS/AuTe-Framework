/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the ATF project
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

package ru.bsc.test.at.executor.step.executor.requester;

import lombok.AllArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.executor.helper.MqMockHelper;
import ru.bsc.test.at.executor.helper.client.api.ClientResponse;
import ru.bsc.test.at.executor.helper.client.impl.mq.ClientMQRequest;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.at.executor.step.executor.ExecutorUtils;
import ru.bsc.test.at.executor.step.executor.scriptengine.JSScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngineProcedureResult;

import java.util.HashMap;
import java.util.Map;

import static ru.bsc.test.at.executor.service.AtProjectExecutor.parseLongOrVariable;

/**
 * @author mobrubov
 * created on 29.12.2018 11:32
 */
@AllArgsConstructor
public abstract class MqAbstractStepRequester implements StepRequester {
    protected StepResult stepResult;
    protected Step step;
    protected String requestBody;
    protected String testId;
    protected Project project;
    protected MqClient mqClient;
    protected Map<String, Object> scenarioVariables;
    protected String projectPath;

    protected abstract ClientResponse call() throws Exception;

    @Override
    public void request() throws Exception {
        ClientResponse responseData = call();

        String content = responseData != null ? responseData.getContent() : "";

        stepResult.setActual(content);
        stepResult.setExpected(step.getExpectedResponse());

        // 4. Сохранить полученные значения
        RequesterUtils.saveValuesByJsonXPath(step, content, scenarioVariables);

        stepResult.setSavedParameters(scenarioVariables.toString());

        // 4.1 Проверить сохраненные значения
        RequesterUtils.checkScenarioVariables(step, scenarioVariables);

        // 5. Подставить сохраненые значения в ожидаемый результат
        String expectedResponse = ExecutorUtils.insertSavedValues(step.getExpectedResponse(), scenarioVariables);
        // 5.1. Расчитать выражения <f></f>
        expectedResponse = ExecutorUtils.evaluateExpressions(expectedResponse, scenarioVariables);
        stepResult.setExpected(expectedResponse);

        RequesterUtils.checkResponseBody(step, expectedResponse, content);
    }

    protected ClientResponse getClientResponse() throws Exception {
        ClientResponse response = null;
        Map<String, Object> generatedProperties = getGeneratedProperties(step);
        // 3. Выполнить запрос (отправить сообщение в очередь)
        String testIdHeaderName = MqMockHelper.convertPropertyCamelPolicy(project.getTestIdHeaderName(), mqClient.isUseCamelNamingPolicyIbmMQ());
        ClientMQRequest clientMQRequest = new ClientMQRequest(step.getMqOutputQueueName(), requestBody, generatedProperties, testId, project.getUseRandomTestId() ? testIdHeaderName: null);
        mqClient.request(clientMQRequest);

        if (StringUtils.isNotBlank(step.getMqInputQueueName())) {
            long calculatedSleep = parseLongOrVariable(scenarioVariables, ExecutorUtils.evaluateExpressions(step.getMqTimeoutMs(), scenarioVariables), 1000);
            response = mqClient.waitMessage(step.getMqInputQueueName(), Math.min(calculatedSleep, mqClient.getMaxTimeoutWait()), project.getUseRandomTestId() ? testIdHeaderName : null, testId);
        }

        // Выполнить скрипт
        if (StringUtils.isNotEmpty(step.getScript())) {
            ScriptEngine scriptEngine = new JSScriptEngine();
            ScriptEngineProcedureResult scriptEngineExecutionResult = scriptEngine.executeProcedure(step.getScript(), scenarioVariables);
            // Привести все переменные сценария к строковому типу
            scenarioVariables.replaceAll((k, v) -> v != null ? String.valueOf(v) : null);

            if (!scriptEngineExecutionResult.isOk()) {
                throw new Exception(scriptEngineExecutionResult.getException());
            }
        }
        return response;
    }


    private Map<String, Object> getGeneratedProperties(Step step) {
        Map<String, Object> generatedProperties = new HashMap<>();
        generatedProperties.put("replyTo", step.getMqInputQueueName());
        return generatedProperties;
    }

}
