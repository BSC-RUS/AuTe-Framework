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
import lombok.extern.slf4j.Slf4j;
import ru.bsc.test.at.executor.helper.client.api.ClientCommonResponse;
import ru.bsc.test.at.executor.helper.client.api.ClientResponse;
import ru.bsc.test.at.executor.helper.client.impl.http.ClientHttpRequest;
import ru.bsc.test.at.executor.helper.client.impl.http.ClientHttpRequestWithVariables;
import ru.bsc.test.at.executor.helper.client.impl.http.HttpClient;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.at.executor.step.executor.ExecutorUtils;
import ru.bsc.test.at.executor.step.executor.scriptengine.JSScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngineProcedureResult;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author Pavel Golovkin
 */
@Slf4j
@AllArgsConstructor
public abstract class RestAbstractStepRequester implements StepRequester {
    protected StepResult stepResult;
    protected Step step;
    protected String requestUrl;
    protected Object requestBody;
    protected Map requestHeaders;
    protected String testId;
    protected Project project;
    protected HttpClient httpClient;
    protected Map<String, Object> scenarioVariables;
    protected String projectPath;

    protected abstract ClientResponse call() throws Exception;

    @Override
    public void request() throws Exception {
        ClientResponse responseData = call();

        stepResult.setActual(responseData.getContent());
        stepResult.setExpected(step.getExpectedResponse());

        // 4. Сохранить полученные значения
        log.debug("Saving scenario variables by JSON XPath");
        RequesterUtils.saveValuesByJsonXPath(step, responseData.getContent(), scenarioVariables);

        stepResult.setSavedParameters(scenarioVariables.toString());
        stepResult.setCookies(httpClient.getCookies().stream().map(cookie -> cookie.getName() + ": " + cookie.getValue()).collect(Collectors.joining(", ")));

        // 4.1 Проверить сохраненные значения
        RequesterUtils.checkScenarioVariables(step,scenarioVariables);

        // 5. Подставить сохраненые значения в ожидаемый результат
        String expectedResponse = ExecutorUtils.insertSavedValues(step.getExpectedResponse(), scenarioVariables);
        // 5.1. Расчитать выражения <f></f>
        expectedResponse = ExecutorUtils.evaluateExpressions(expectedResponse, scenarioVariables);
        stepResult.setExpected(expectedResponse);

        // 6.1. Сохранить statusCode в результат степа
        int statusCode = responseData.getStatusCode();
        stepResult.setStatus(String.valueOf(statusCode));

        // 6.2. Проверить код статуса ответа
        Integer expectedStatusCode = step.getExpectedStatusCode();
        log.debug("Expected status is {} and actual status is {}", expectedStatusCode, responseData.getStatusCode());
        if (expectedStatusCode != null && expectedStatusCode != responseData.getStatusCode()) {
            throw new Exception(String.format(
                    "Expected status code: %d. Actual status code: %d",
                    expectedStatusCode,
                    responseData.getStatusCode()
            ));
        }

        RequesterUtils.compareResponse(step, expectedResponse, responseData);
    }

    protected ClientResponse getClientResponse(RequestData requestData) throws Exception {
        ClientCommonResponse responseData;
        log.debug("Executing http request");
        if (step.getRequestBodyType() == null || RequestBodyType.JSON.equals(step.getRequestBodyType())) {
            ClientHttpRequest clientHttpRequest = ClientHttpRequest.defaultBuilder()
                        .url(requestUrl)
                        .body(requestBody)
                        .method(step.getRequestMethod())
                        .testId(testId)
                        .headers(requestHeaders)
                        .testIdHeaderName(project.getTestIdHeaderName())
                        .useResponseAsBase64(step.isUseResponseAsBase64())
                        .build();
            responseData = httpClient.request(clientHttpRequest);
        } else {
            if (step.getFormDataList() == null) {
                step.setFormDataList(Collections.emptyList());
            }
            stepResult.setRequestBody(
                    step.getFormDataList()
                            .stream()
                            .map(formData -> {
                                String result = formData.getFieldName() + " = ";
                                if (FieldType.TEXT.equals(formData.getFieldType()) || formData.getFieldType() == null) {
                                    result += ExecutorUtils.insertSavedValues(formData.getValue(), scenarioVariables);
                                } else {
                                    result += (projectPath == null ? "" : projectPath) + formData.getFilePath();
                                }
                                return result;
                            })
                            .collect(Collectors.joining("\r\n")));
            ClientHttpRequestWithVariables clientHttpRequest = ClientHttpRequestWithVariables.builderWithVariables()
                            .url(requestUrl)
                            .body(requestBody)
                            .method(step.getRequestMethod())
                            .testId(testId)
                            .headers(requestHeaders)
                            .testIdHeaderName(project.getTestIdHeaderName())
                            .useResponseAsBase64(step.isUseResponseAsBase64())
                            .scenarioVariables(scenarioVariables)
                            .projectPath(projectPath)
                            .step(step)
                            .build();
            responseData = httpClient.request(clientHttpRequest);
        }
        requestData.setRequestBody(stepResult.getRequestBody());
        requestData.setResponseBody(responseData.getContent());

        // Выполнить скрипт
        log.debug("Executing script {}", step.getScript());
        if (isNotEmpty(step.getScript())) {
            ScriptEngine scriptEngine = new JSScriptEngine(responseData);
            ScriptEngineProcedureResult scriptEngineExecutionResult = scriptEngine.executeProcedure(step.getScript(), scenarioVariables);
            if (!scriptEngineExecutionResult.isOk()) {
                throw new Exception(scriptEngineExecutionResult.getException());
            }
        }
        return responseData;
    }
}