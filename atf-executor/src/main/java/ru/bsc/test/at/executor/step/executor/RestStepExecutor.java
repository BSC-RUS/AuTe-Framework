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
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.helper.client.impl.http.HttpClient;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.Stand;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.at.executor.service.DelayUtilities;
import ru.bsc.test.at.executor.step.executor.requester.RestPollingStepRequester;
import ru.bsc.test.at.executor.step.executor.requester.RestSimpleStepRequester;
import ru.bsc.test.at.executor.step.executor.requester.StepRequester;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.util.*;

import static ru.bsc.test.at.executor.model.StepMode.REST;
import static ru.bsc.test.at.executor.model.StepMode.REST_ASYNC;
import static ru.bsc.test.at.executor.service.AtProjectExecutor.parseLongOrVariable;

@Slf4j
public class RestStepExecutor implements IStepExecutor {
    private final DelayUtilities delayUtilities = new DelayUtilities();

    @Override
    public void execute(WireMockAdmin wireMockAdmin, Connection connection, Stand stand, HttpClient httpClient, MqClient mqClient, Map<String, Object> scenarioVariables, String testId, Project project, Scenario scenario, Step step, StepResult stepResult, String projectPath) throws Exception {

        log.debug("Executing test step {} {} {} {} {}", stand, scenarioVariables, testId, project, step);
        stepResult.setSavedParameters(scenarioVariables.toString());

        // 0. Установить ответы сервисов, которые будут использоваться в WireMock для определения ответа
        ExecutorUtils.setMockResponses(wireMockAdmin, project, testId, step.getMockServiceResponseList(), step.getCode(), scenario.getName(), scenarioVariables);

        // 0.1 Установить ответы для имитации внешних сервисов, работающих через очереди сообщений
        ExecutorUtils.setMqMockResponses(wireMockAdmin, testId, step.getMqMockResponseList(), scenarioVariables);

        // 1. Выполнить запрос БД и сохранить полученные значения
        ExecutorUtils.executeSql(connection, step, scenarioVariables, stepResult);
        stepResult.setSavedParameters(scenarioVariables.toString());

        // 1.1 Отправить сообщение в очередь
        ExecutorUtils.sendMessagesToQuery(project, step, scenarioVariables, mqClient, project.getTestIdHeaderName(), testId);

        // 2. Подстановка сохраненных параметров в строку запроса

        String requestUrl = getRequestUrl(step, stand, scenarioVariables);
        stepResult.setRequestUrl(requestUrl);

        // 2.1 Подстановка сохраненных параметров в тело запроса
        String requestBody = ExecutorUtils.insertSavedValues(step.getRequest(), scenarioVariables);

        // 2.2 Вычислить функции в теле запроса
        requestBody = ExecutorUtils.evaluateExpressions(requestBody, scenarioVariables);
        stepResult.setRequestBody(requestBody);

        // 2.3 Подстановка переменных сценария в заголовки запроса
        Map<String, String> requestHeaders = generateHeaders(step.getRequestHeaders(), scenarioVariables);

        // 2.4 Cyclic sending request, COM-84
        long numberRepetitions = parseLongOrVariable(scenarioVariables, step.getNumberRepetitions(), 1);
        numberRepetitions = numberRepetitions > 300 ? 300 : numberRepetitions;

        stepResult.setRequestDataList(new LinkedList<>());
        for (int repetitionCounter = 0; repetitionCounter < numberRepetitions; repetitionCounter++) {

            // COM-123 Timeout
            if (step.getTimeoutMs() != null && !step.getTimeoutMs().isEmpty() && (step.isTimeoutEachRepetition())) {
                delayUtilities.delay(parseLongOrVariable(scenarioVariables, step.getTimeoutMs(), 0));
            }

            log.debug("Polling repetitionCounter={} numberRepetitions={}", repetitionCounter, numberRepetitions);

            StepRequester stepRequester;
            if (step.getUsePolling()) {
                stepRequester = new RestPollingStepRequester(stepResult, step, requestUrl, requestBody, requestHeaders, testId, project, httpClient, scenarioVariables, projectPath);
            } else {
                stepRequester = new RestSimpleStepRequester(stepResult, step, requestUrl, requestBody, requestHeaders, testId, project, httpClient, scenarioVariables, projectPath);
            }
            stepRequester.request();
        }
    }

    @Override
    public boolean support(Step step) {
        return REST.equals(step.getStepMode()) || REST_ASYNC.equals(step.getStepMode());
    }

    private String getRequestUrl(Step step, Stand stand, Map<String, Object> scenarioVariables) {
        if (StringUtils.isEmpty(step.getRelativeUrl())) {
            return stand.getServiceUrl();
        }
        String stepUrl = ExecutorUtils.insertSavedValues(step.getRelativeUrl(), scenarioVariables);
        stepUrl = encodeUrl(stepUrl);
        try {
            return new URI(stepUrl).isAbsolute() ? stepUrl : stand.getServiceUrl() + stepUrl;
        } catch (URISyntaxException e) {
            return stand.getServiceUrl() + stepUrl;
        }
    }

    private String encodeUrl(String url) {
        if (url != null && url.contains("?")) {
            int startParam = url.indexOf("?");
            String baseUrl = url.substring(0, startParam + 1);
            String paramsUrl = url.substring(startParam + 1, url.length());
            List<NameValuePair> paramsList = URLEncodedUtils.parse(paramsUrl, StandardCharsets.UTF_8);
            paramsUrl = URLEncodedUtils.format(paramsList, StandardCharsets.UTF_8);
            return baseUrl + paramsUrl;
        }
        return url;
    }

    private Map<String, String> generateHeaders(String template, Map<String, Object> scenarioVariables) {
        //TODO fix неоптимальная работа с параметрами
        String headersStr = ExecutorUtils.insertSavedValues(template, scenarioVariables);
        if (StringUtils.isEmpty(headersStr)) {
            //Возвращаем пустые хедеры
            return new HashMap<>();
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
}
