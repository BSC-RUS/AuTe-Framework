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

package ru.bsc.test.at.executor.helper;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xmlunit.XMLUnitException;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.MockedRequest;
import ru.bsc.test.at.executor.exception.ComparisonException;
import ru.bsc.test.at.executor.exception.JsonParsingException;
import ru.bsc.test.at.executor.exception.UnMockedRequestsException;
import ru.bsc.test.at.executor.model.ExpectedMqRequest;
import ru.bsc.test.at.executor.model.ExpectedRequestResult;
import ru.bsc.test.at.executor.model.RequestResult;
import ru.bsc.test.at.executor.model.ScenarioVariableFromMqRequest;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.at.executor.service.AtProjectExecutor;
import ru.bsc.test.at.executor.step.executor.ExecutorUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.BooleanUtils.isNotTrue;
import static org.springframework.util.CollectionUtils.isEmpty;
import static ru.bsc.test.at.executor.utils.StreamUtils.nullSafeStream;

@Slf4j
public class MqMockHelper {

    private static final String HYPHEN = "_HYPHEN_";
    private static final String DOT = "_DOT_";

    public static String convertPropertyCamelPolicy(String testIdHeaderNameProperty, boolean useCamelNaming) {
        return useCamelNaming && !StringUtils.isEmpty(testIdHeaderNameProperty) ?
               testIdHeaderNameProperty.replace("-", HYPHEN).replace(".", DOT) : testIdHeaderNameProperty;
    }

    public void assertMqRequests(WireMockAdmin mqMockerAdmin, String testId, Step step, Map<String, Object> scenarioVariables, Integer mqCheckCount,
            Long mqCheckInterval, StepResult stepResult) throws Exception {
        if (mqMockerAdmin == null) {
            return;
        }

        int maxCount = Math.min(mqCheckCount != null ? mqCheckCount : 10, 30);
        long sleepInterval = Math.min(mqCheckInterval != null ? mqCheckInterval : 500L, 5000L);

        if (isEmpty(step.getExpectedMqRequestList())) {
            try {
                List<MockedRequest> actualMqRequestList = null;
                int counter = 0;
                while (counter < maxCount && isEmpty(actualMqRequestList)) {
                    if (isEmpty(actualMqRequestList)) {
                        Thread.sleep(sleepInterval);
                    }
                    actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);
                    counter++;
                }

                putRequestResults(stepResult, actualMqRequestList, step.getExpectedMqRequestList(), scenarioVariables);

                if (isNotTrue(step.getIgnoreUndeclaredMqRequests()) && !isEmpty(actualMqRequestList)) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Sent request without mock : \n");
                    actualMqRequestList.forEach(r -> sb.append("queue: ").append(r.getSourceQueue()).append("body: ").append(r.getRequestBody()).append("\n"));
                    throw new UnMockedRequestsException(sb.toString());
                }

            } catch (JsonParseException ex) {
                // DO NOTING (не во всех wiremock есть mq)
            }
            return;
        }

        List<MockedRequest> actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);
        List<MockedRequest> unMockedRequests = Collections.emptyList();
        Map<ExpectedMqRequest, List<MockedRequest>> incorrectExpectedActualMultimap = new HashMap<>();
        for (int counter = 0; counter < maxCount; counter++) {
            actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);
            List<MockedRequest> actualMqRequestListF = actualMqRequestList;
            if (isNotTrue(step.getIgnoreRequestsInvocations())) { // если не стоит галочка и есть запросы для которых нет заглушек, то запоминаем и ругаемся
                unMockedRequests = actualMqRequestList.stream()
                    .filter(actual -> step.getExpectedMqRequestList().stream().filter(exp -> isSame(actual, exp, scenarioVariables)).count() == 0)
                    .collect(toList());
                if (!isEmpty(unMockedRequests)) {
                    break;
                }
            }

            incorrectExpectedActualMultimap = new HashMap<>();
            Map<ExpectedMqRequest, List<MockedRequest>> incorrectExpectedActualMultimapF = incorrectExpectedActualMultimap;
            step.getExpectedMqRequestList().forEach(exp -> {
                List<MockedRequest> actualList = actualMqRequestListF.stream().filter(actual -> isSame(actual, exp, scenarioVariables)).collect(toList());
                long expectedCount = AtProjectExecutor.parseLongOrVariable(scenarioVariables,
                        ExecutorUtils.evaluateExpressions(exp.getCount(), scenarioVariables),
                        1
                );
                if (expectedCount != actualList.size()) {
                    incorrectExpectedActualMultimapF.put(exp, actualList);
                }
            });
            // тут все нормально, дальше ничего не ждем,
            // TODO - подстава будет в том слечае, если у нас WM узнает о новом сообщении после проверки, эту ситуацию обработать нельзя,
            // как вариант можно дольше ждать, но тогда сценарии будут выпольняться очень долго
            // что делать пока не понятно
            if (incorrectExpectedActualMultimap.isEmpty()) {
                break;
            }

            Thread.sleep(sleepInterval);
        }

        if (step.getScenarioVariableFromMqRequestList() != null) {
            for (ScenarioVariableFromMqRequest variable : step.getScenarioVariableFromMqRequestList()) {
                MockedRequest actual = actualMqRequestList.stream()
                    .filter(mockedRequest -> Objects.equals(mockedRequest.getSourceQueue(), variable.getSourceQueue()))
                    .findAny()
                    .orElse(null);
                if (actual != null) {
                    scenarioVariables.put(
                            variable.getVariableName().trim(),
                            XMLUtils.getValueByXPath(actual.getRequestBody(), variable.getXpath())
                    );
                }
            }
        }

        putRequestResults(stepResult, actualMqRequestList, step.getExpectedMqRequestList(), scenarioVariables);

        for (ExpectedMqRequest expectedMqRequest : step.getExpectedMqRequestList()) {
            MockedRequest actualRequest = actualMqRequestList.stream()
                .filter(mockedRequest -> mockedRequest.getSourceQueue().equals(expectedMqRequest.getSourceQueue()))
                .findAny().orElse(null);
            if (actualRequest != null) {
                actualMqRequestList.remove(actualRequest);

                ComparisonResult comparisonResult = compareRequestsBody(expectedMqRequest, actualRequest, scenarioVariables);
                if (comparisonResult.isHasDifferences()) {
                    updateDiffStatus(stepResult, actualRequest);
                    throw new ComparisonException(
                            comparisonResult.getDiff(),
                            comparisonResult.getExpectedRequestBody(),
                            actualRequest.getRequestBody()
                    );
                }
            } else {
                stepResult.getUncalledExpectedRequests().add(transform(expectedMqRequest, 1));
                throw new Exception(String.format("Queue %s is not called", expectedMqRequest.getSourceQueue()));
            }
        }

        if (!isEmpty(incorrectExpectedActualMultimap)) {
            StringBuilder message = new StringBuilder();
            for (Map.Entry<ExpectedMqRequest, List<MockedRequest>> entry : incorrectExpectedActualMultimap.entrySet()) {
                message.append("\n");
                long expectedCount = AtProjectExecutor.parseLongOrVariable(scenarioVariables,
                        ExecutorUtils.evaluateExpressions(entry.getKey().getCount(), scenarioVariables),
                        1
                );
                // Вызвать ошибку: не совпадает количество вызовов сервисов
                message.append(String.format(
                        "Invalid number of JMS requests: expected: %d, actual: %d%nActual requests:%n",
                        expectedCount,
                        entry.getValue().size()
                ));
                stepResult.getUncalledExpectedRequests().add(transform(entry.getKey(), (int) expectedCount - entry.getValue().size()));
                int i = 0;
                for (MockedRequest actual : entry.getValue()) {
                    message.append(String.format(
                            " %d: Source queue: %s; Destination queue: %s",
                            i + 1,
                            actual.getSourceQueue(),
                            actual.getDestinationQueue()
                    ));
                }
            }
            throw new Exception(message.toString());
        }

        if (!isEmpty(unMockedRequests)) {
            StringBuilder sb = new StringBuilder();
            sb.append("Sent request without mock : \n");
            unMockedRequests.forEach(r -> sb.append("queue: ").append(r.getSourceQueue()).append("body: ").append(r.getRequestBody()).append("\n"));
            throw new UnMockedRequestsException(sb.toString());
        }

    }

    private void putRequestResults(StepResult stepResult, List<MockedRequest> actualMqRequestList, List<ExpectedMqRequest> expectedMqRequests, Map<String, Object> scenarioVariables) {
        stepResult.getExpectedRequestResults().addAll(nullSafeStream(actualMqRequestList)
            .map(actual -> {
                String expectedRequest = nullSafeStream(expectedMqRequests)
                    .filter(ex -> isSame(actual, ex, scenarioVariables))
                    .map(ExpectedMqRequest::getRequestBody)
                    .findFirst().orElse(null);
                return new ExpectedRequestResult(expectedRequest != null, expectedRequest, transform(actual), false);
            }).collect(toList()));
    }

    private void updateDiffStatus(StepResult stepResult, MockedRequest actualMqRequest) {
        stepResult.getExpectedRequestResults().stream()
            .filter(r -> r.equalsActualRequest(actualMqRequest))
            .findFirst()
            .ifPresent(r -> r.setHasDiff(true));
    }

    private RequestResult transform(MockedRequest mockedRequest) {
        RequestResult result = new RequestResult();
        result.setBody(mockedRequest.getRequestBody());
        result.setLoggedDate(mockedRequest.getDate());
        result.setMethod("MQ");
        result.setSource(mockedRequest.getSourceQueue());
        return result;
    }

    private RequestResult transform(ExpectedMqRequest expected, int count) {
        RequestResult result = new RequestResult();
        result.setBody(expected.getRequestBody());
        result.setMethod("MQ");
        result.setSource(expected.getSourceQueue());
        result.setCount(count);
        return result;
    }

    protected ComparisonResult compareRequestsBody(ExpectedMqRequest expectedMqRequest, MockedRequest actualRequest, Map<String, Object> scenarioVariables) {
        Set<String> ignoredTags;
        if (expectedMqRequest.getIgnoredTags() != null) {
            ignoredTags = new HashSet<>(Arrays.stream(expectedMqRequest.getIgnoredTags()
                .split(","))
                .map(String::trim)
                .collect(toList()));
        } else {
            ignoredTags = new HashSet<>();
        }

        String expectedRequestBody = ExecutorUtils.evaluateExpressions(
                ExecutorUtils.insertSavedValues(expectedMqRequest.getRequestBody(), scenarioVariables),
                scenarioVariables);
        String actualRequestBody = actualRequest.getRequestBody();

        ComparisonResult comparisonResult = null;
        try {
            comparisonResult = CompareUtils.compareRequestAsXml(expectedRequestBody, actualRequestBody, ignoredTags);
        } catch (XMLUnitException xUnitEx) {
            log.info("Exception while compare mq request as XML. Trying parse as json");
            log.debug("Detached exception", xUnitEx);
            try {
                comparisonResult = CompareUtils.compareRequestAsJson(expectedRequestBody, actualRequestBody, ignoredTags, null);
            } catch (JsonParsingException jsonEx) {
                log.info("Exception while compare mq request as JSON. Trying parse as string");
                log.debug("Detached exception", jsonEx);
                try {
                    comparisonResult = CompareUtils.compareRequestAsString(expectedRequestBody, actualRequestBody);
                } catch (ComparisonException strEx) {
                    log.debug("Exception while compare mq request as String", strEx);
                }
            }
        }

        return comparisonResult;
    }

    private boolean isSame(MockedRequest mockedRequest, ExpectedMqRequest expected, Map<String, Object> scenarioVariables) {
        if (!Objects.equals(expected.getSourceQueue(), mockedRequest.getSourceQueue())) {
            return false;
        }
        ComparisonResult result = compareRequestsBody(expected, mockedRequest, scenarioVariables);
        return !result.isHasDifferences();
    }

}
