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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.MockedRequest;
import ru.bsc.test.at.executor.exception.ComparisonException;
import ru.bsc.test.at.executor.model.ExpectedMqRequest;
import ru.bsc.test.at.executor.model.ScenarioVariableFromMqRequest;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.service.AtProjectExecutor;
import ru.bsc.test.at.executor.step.executor.ExecutorUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MqMockHelper {

    private static final String HYPHEN = "_HYPHEN_";
    private static final String DOT = "_DOT_";


    public static String convertPropertyCamelPolicy(String testIdHeaderNameProperty, boolean useCamelNaming) {
        return useCamelNaming && !StringUtils.isEmpty(testIdHeaderNameProperty) ?
                testIdHeaderNameProperty.replace("-", HYPHEN).replace(".", DOT) : testIdHeaderNameProperty;
    }


    public void assertMqRequests(WireMockAdmin mqMockerAdmin, String testId, Step step, Map<String, Object> scenarioVariables, Integer mqCheckCount, Long mqCheckInterval) throws Exception {
        if (mqMockerAdmin == null) {
            return;
        }

        if (step.getExpectedMqRequestList() == null || step.getExpectedMqRequestList().isEmpty()) {
            return;
        }

        List<ExpectedMqRequest> expectedMqRequestList = new LinkedList<>();
        step.getExpectedMqRequestList().forEach(expectedMqRequest -> {
            long count = AtProjectExecutor.parseLongOrVariable(scenarioVariables, ExecutorUtils.evaluateExpressions(expectedMqRequest.getCount(), scenarioVariables), 1);;
            for (int i = 0; i < count; i++) {
                expectedMqRequestList.add(expectedMqRequest);
            }
        });

        List<MockedRequest> actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);

        for (int counter = 0; counter < Math.min(mqCheckCount != null ? mqCheckCount : 10, 30) && isMqRequestsCountIncorrect(step, expectedMqRequestList, actualMqRequestList); counter++) {
            Thread.sleep(Math.min(mqCheckInterval != null ? mqCheckInterval : 500L, 5000L));
            actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);
        }

        if (isMqRequestsCountIncorrect(step, expectedMqRequestList, actualMqRequestList)) {
            // Вызвать ошибку: не совпадает количество вызовов сервисов
            StringBuilder message = new StringBuilder();
            message.append(String.format(
                    "Invalid number of JMS requests: expected: %d, actual: %d%nActual requests:%n",
                    expectedMqRequestList.size(),
                    actualMqRequestList.size()
            ));
            for (int i = 0; i < actualMqRequestList.size(); i++) {
                MockedRequest request = actualMqRequestList.get(i);
                message.append(String.format(
                        " %d: Source queue: %s; Destination queue: %s",
                        i + 1,
                        request.getSourceQueue(),
                        request.getDestinationQueue()));
            }
            throw new Exception(message.toString());
        }

        if (step.getScenarioVariableFromMqRequestList() != null) {
            for (ScenarioVariableFromMqRequest variable : step.getScenarioVariableFromMqRequestList()) {
                MockedRequest actual = actualMqRequestList.stream()
                        .filter(mockedRequest -> Objects.equals(mockedRequest.getSourceQueue(), variable.getSourceQueue()))
                        .findAny()
                        .orElse(null);
                if (actual != null) {
                    scenarioVariables.put(variable.getVariableName().trim(), XMLUtils.getValueByXPath(actual.getRequestBody(), variable.getXpath()));
                }
            }
        }

        for (ExpectedMqRequest expectedMqRequest : expectedMqRequestList) {
            MockedRequest actualRequest = actualMqRequestList.stream()
                    .filter(mockedRequest -> mockedRequest.getSourceQueue().equals(expectedMqRequest.getSourceQueue()))
                    .findAny().orElse(null);
            if (actualRequest != null) {
                actualMqRequestList.remove(actualRequest);

                compareRequest(
                        ExecutorUtils.evaluateExpressions(ExecutorUtils.insertSavedValues(expectedMqRequest.getRequestBody(), scenarioVariables), scenarioVariables),
                        actualRequest.getRequestBody(),
                        expectedMqRequest.getIgnoredTags() != null ?
                                new HashSet<>(Arrays.stream(expectedMqRequest.getIgnoredTags()
                                        .split(","))
                                        .map(String::trim)
                                        .collect(Collectors.toList())) : null
                );
            } else {
                throw new Exception(String.format("Queue %s is not called", expectedMqRequest.getSourceQueue()));
            }
        }
    }

    private void compareRequest(String expectedRequest, String actualRequest, Set<String> ignoredTags) throws ComparisonException {
        Diff diff = DiffBuilder.compare(expectedRequest)
                .withTest(actualRequest)
                .checkForIdentical()
                .ignoreComments()
                .ignoreWhitespace()
                .withDifferenceEvaluator(new IgnoreTagsDifferenceEvaluator(ignoredTags))
                .build();

        if (diff.hasDifferences()) {
            throw new ComparisonException(diff, expectedRequest, actualRequest);
        }
    }

    private boolean isMqRequestsCountIncorrect(Step step, List expectedMqRequests, List actualMqRequests) {
        return step.getIgnoreUndeclaredMqRequests() ?
                expectedMqRequests.size() > actualMqRequests.size() :
                expectedMqRequests.size() != actualMqRequests.size();
    }
}
