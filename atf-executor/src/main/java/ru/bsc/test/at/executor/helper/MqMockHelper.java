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
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.MockedRequest;
import ru.bsc.test.at.executor.exception.ComparisonException;
import ru.bsc.test.at.executor.model.ExpectedMqRequest;
import ru.bsc.test.at.executor.model.ScenarioVariableFromMqRequest;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.service.AtProjectExecutor;
import ru.bsc.test.at.executor.step.executor.AbstractStepExecutor;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MqMockHelper {

    public void assertMqRequests(WireMockAdmin mqMockerAdmin, String testId, Step step, Map<String, Object> scenarioVariables, Integer mqCheckCount, Long mqCheckInterval) throws Exception {
        if (mqMockerAdmin == null) {
            return;
        }

        if (step.getExpectedMqRequestList() == null || step.getExpectedMqRequestList().isEmpty()) {
            return;
        }

        List<ExpectedMqRequest> expectedMqRequestList = new LinkedList<>();
        step.getExpectedMqRequestList().forEach(expectedMqRequest -> {

            long count = 0;
            try {
                count = AtProjectExecutor.parseLongOrVariable(scenarioVariables, AbstractStepExecutor.evaluateExpressions(expectedMqRequest.getCount(), scenarioVariables, null), 1);
            } catch (ScriptException e) {
                log.error("{}", e);
            }
            for (int i = 0; i < count; i++) {
                expectedMqRequestList.add(expectedMqRequest);
            }
        });

        List<MockedRequest> actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);

        for (int counter = 0; counter < Math.min(mqCheckCount != null ? mqCheckCount : 10, 30) && expectedMqRequestList.size() != actualMqRequestList.size(); counter++) {
            Thread.sleep(Math.min(mqCheckInterval != null ? mqCheckInterval : 500L, 5000L));
            actualMqRequestList = mqMockerAdmin.getMqRequestListByTestId(testId);
        }

        if (expectedMqRequestList.size() != actualMqRequestList.size()) {
            // Вызвать ошибку: не совпадает количество вызовов сервисов
            throw new Exception(String.format(
                    "Invalid number of JMS requests: expected: %d, actual: %d",
                    expectedMqRequestList.size(),
                    actualMqRequestList.size()
            ));
        }

        if (step.getScenarioVariableFromMqRequestList() != null) {
            for (ScenarioVariableFromMqRequest variable : step.getScenarioVariableFromMqRequestList()) {
                MockedRequest actual = actualMqRequestList.stream()
                        .filter(mockedRequest -> Objects.equals(mockedRequest.getSourceQueue(), variable.getSourceQueue()))
                        .findAny()
                        .orElse(null);
                if (actual != null) {

                    // TODO Вынести работу с xml-документом в отдельный класс
                    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder builder = builderFactory.newDocumentBuilder();
                    Document xmlDocument = builder.parse(new InputSource(new StringReader(actual.getRequestBody())));
                    XPath xPath = XPathFactory.newInstance().newXPath();
                    String valueFromMock = xPath.compile(variable.getXpath()).evaluate(xmlDocument);

                    scenarioVariables.put(variable.getVariableName().trim(), valueFromMock);
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
                        AbstractStepExecutor.evaluateExpressions(AbstractStepExecutor.insertSavedValues(expectedMqRequest.getRequestBody(), scenarioVariables), scenarioVariables, null),
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
}
