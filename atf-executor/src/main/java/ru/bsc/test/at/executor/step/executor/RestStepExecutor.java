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
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.helper.client.api.ClientCommonResponse;
import ru.bsc.test.at.executor.helper.client.impl.http.ClientHttpRequest;
import ru.bsc.test.at.executor.helper.client.impl.http.ClientHttpRequestWithVariables;
import ru.bsc.test.at.executor.helper.client.impl.http.HttpClient;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.at.executor.step.executor.scriptengine.JSScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngineProcedureResult;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ru.bsc.test.at.executor.service.AtProjectExecutor.parseLongOrVariable;

@Slf4j
public class RestStepExecutor extends AbstractStepExecutor {

    private final static int POLLING_RETRY_COUNT = 50;

    @Override
    public void execute(WireMockAdmin wireMockAdmin, Connection connection, Stand stand, HttpClient httpClient, MqClient mqClient, Map<String, Object> scenarioVariables, String testId, Project project, Step step, StepResult stepResult, String projectPath) throws Exception {

        log.debug("Executing test step {} {} {} {}", stand, scenarioVariables, testId, project, step);
        stepResult.setSavedParameters(scenarioVariables.toString());

        // 0. Установить ответы сервисов, которые будут использоваться в WireMock для определения ответа
        setMockResponses(wireMockAdmin, project, testId, step.getMockServiceResponseList());

        // 0.1 Установить ответы для имитации внешних сервисов, работающих через очереди сообщений
        setMqMockResponses(wireMockAdmin, testId, step.getMqMockResponseList(), scenarioVariables);

        // 1. Выполнить запрос БД и сохранить полученные значения
        executeSql(connection, step, scenarioVariables, stepResult);
        stepResult.setSavedParameters(scenarioVariables.toString());

        // 1.1 Отправить сообщение в очередь
        sendMessagesToQuery(project, step, scenarioVariables, mqClient, project.getTestIdHeaderName(), testId);

        // 2. Подстановка сохраненных параметров в строку запроса
        String requestUrl = stand.getServiceUrl() + insertSavedValuesToURL(step.getRelativeUrl(), scenarioVariables);
        stepResult.setRequestUrl(requestUrl);

        // 2.1 Подстановка сохраненных параметров в тело запроса
        String requestBody = insertSavedValues(step.getRequest(), scenarioVariables);

        // 2.2 Вычислить функции в теле запроса
        requestBody = evaluateExpressions(requestBody, scenarioVariables, null);
        stepResult.setRequestBody(requestBody);

        // 2.3 Подстановка переменных сценария в заголовки запроса
        Map requestHeaders = generateHeaders(step.getRequestHeaders(), scenarioVariables);

        // 2.4 Cyclic sending request, COM-84
        long numberRepetitions = parseLongOrVariable(scenarioVariables, step.getNumberRepetitions(), 1);
        numberRepetitions = numberRepetitions > 300 ? 300 : numberRepetitions;

        stepResult.setRequestDataList(new LinkedList<>());
        for (int repetitionCounter = 0; repetitionCounter < numberRepetitions; repetitionCounter++) {
            log.debug("Polling repetitionCounter={} numberRepetitions={}", repetitionCounter, numberRepetitions);

            // Polling
            int retryCounter = 0;
            boolean retry = false;

            ClientCommonResponse responseData;
            do {
                RequestData requestData = new RequestData();
                stepResult.getRequestDataList().add(requestData);

                retryCounter++;
                // 3. Выполнить запрос
                log.debug("Executing http request");
                if (step.getRequestBodyType() == null || RequestBodyType.JSON.equals(step.getRequestBodyType())) {
                    ClientHttpRequest clientHttpRequest = new ClientHttpRequest(
                            requestUrl, requestBody, step.getRequestMethod(), requestHeaders,
                            testId, project.getTestIdHeaderName());
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
                                            result += insertSavedValues(formData.getValue(), scenarioVariables);
                                        } else {
                                            result += (projectPath == null ? "" : projectPath) + formData.getFilePath();
                                        }
                                        return result;
                                    })
                                    .collect(Collectors.joining("\r\n")));
                    ClientHttpRequestWithVariables clientHttpRequest = new ClientHttpRequestWithVariables(
                            requestUrl, requestBody, step.getRequestMethod(), requestHeaders,
                            testId, project.getTestIdHeaderName(), scenarioVariables, projectPath, step);
                    responseData = httpClient.request(clientHttpRequest);
                }
                requestData.setRequestBody(stepResult.getRequestBody());
                requestData.setResponseBody(responseData.getContent());

                // Выполнить скрипт
                log.debug("Executing script {}", step.getScript());
                if (isNotEmpty(step.getScript())) {
                    ScriptEngine scriptEngine = new JSScriptEngine();
                    ScriptEngineProcedureResult scriptEngineExecutionResult = scriptEngine.executeProcedure(step.getScript(), scenarioVariables);
                    if (!scriptEngineExecutionResult.isOk()) {
                        throw new Exception(scriptEngineExecutionResult.getException());
                    }
                }

                // 3.1. Polling
                if (step.getUsePolling()) {
                    retry = tryUsePolling(step, responseData);
                }
            } while (retry && retryCounter <= POLLING_RETRY_COUNT);

            stepResult.setPollingRetryCount(retryCounter);
            stepResult.setActual(responseData.getContent());
            stepResult.setExpected(step.getExpectedResponse());

            // 4. Сохранить полученные значения
            log.debug("Saving scenario variables by JSON XPath");
            saveValuesByJsonXPath(step, responseData.getContent(), scenarioVariables);

            stepResult.setSavedParameters(scenarioVariables.toString());
            stepResult.setCookies(httpClient.getCookies().stream().map(cookie -> cookie.getName() + ": " + cookie.getValue()).collect(Collectors.joining(", ")));

            // 4.1 Проверить сохраненные значения
            if (step.getSavedValuesCheck() != null) {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry<String, String> entry : step.getSavedValuesCheck().entrySet()) {
                    String valueExpected = entry.getValue() == null ? "" : entry.getValue();
                    for (Map.Entry<String, Object> savedVal : scenarioVariables.entrySet()) {
                        String key = String.format("%%%s%%", savedVal.getKey());
                        valueExpected = valueExpected.replaceAll(key, String.valueOf(savedVal.getValue()));
                    }
                    String valueActual = String.valueOf(scenarioVariables.get(entry.getKey()));
                    if (!valueExpected.equals(valueActual)) {
                        sb.append("\nSaved value ").append(entry.getKey()).append(" = ").append(valueActual).append(". Expected: ").append(valueExpected);
                    }
                }
                log.debug("Test step body {}", sb);
                if (sb.length() > 0) {
                    throw new Exception(sb.toString());
                }
            }

            // 5. Подставить сохраненые значения в ожидаемый результат
            String expectedResponse = insertSavedValues(step.getExpectedResponse(), scenarioVariables);
            // 5.1. Расчитать выражения <f></f>
            expectedResponse = evaluateExpressions(expectedResponse, scenarioVariables, responseData);
            stepResult.setExpected(expectedResponse);

            // 6. Проверить код статуса ответа
            Integer expectedStatusCode = step.getExpectedStatusCode();
            log.debug("Expected status is {} and actual status is {}", expectedStatusCode, responseData.getStatusCode());
            if ((expectedStatusCode != null) && (expectedStatusCode != responseData.getStatusCode())) {
                throw new Exception(String.format(
                        "Expected status code: %d. Actual status code: %d",
                        expectedStatusCode,
                        responseData.getStatusCode()
                ));
            }

            compareResponse(step, expectedResponse, responseData);
        }

        // 7. Прочитать, что тестируемый сервис отправлял в REST-заглушку.
        log.debug("Read REST mock data");
        parseMockRequests(project, step, wireMockAdmin, scenarioVariables, testId);
    }

    private Map generateHeaders(String template, Map<String, Object> scenarioVariables) {
        //TODO fix неоптимальная работа с параметрами
        String headersStr = insertSavedValues(template, scenarioVariables);
        if (StringUtils.isEmpty(headersStr)) {
            //Возвращаем пустые хедеры
            return new HashMap();
        }
        Map<String, String> headers = new HashMap<>();
        try (Scanner scanner = new Scanner(headersStr)) {
            while (scanner.hasNextLine()) {
                String header = scanner.nextLine();
                String[] headerDetail = header.split(":");
                if (headerDetail.length >= 2) {
                    headers.put(headerDetail[0].trim(), headerDetail[1].trim());
                }
            }
        }
        return headers;
    }

    @Override
    public boolean support(Step step) {
        return step.getStepMode() == null || StepMode.REST.equals(step.getStepMode());
    }
}
