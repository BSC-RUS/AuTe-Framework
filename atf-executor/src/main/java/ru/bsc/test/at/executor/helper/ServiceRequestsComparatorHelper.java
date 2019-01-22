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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.Customization;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.xml.sax.SAXParseException;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.MockRequest;
import ru.bsc.test.at.executor.ei.wiremock.model.RequestMatcher;
import ru.bsc.test.at.executor.ei.wiremock.model.WireMockRequest;
import ru.bsc.test.at.executor.exception.ComparisonException;
import ru.bsc.test.at.executor.exception.InvalidNumberOfMockRequests;
import ru.bsc.test.at.executor.exception.JsonParsingException;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.at.executor.service.AtProjectExecutor;
import ru.bsc.test.at.executor.service.DelayUtilities;
import ru.bsc.test.at.executor.step.executor.ExecutorUtils;
import ru.bsc.test.at.executor.validation.IgnoringComparator;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Created by sdoroshin on 30.05.2017.
 */
@Slf4j
public class ServiceRequestsComparatorHelper {
    private static final String IGNORE = "\\u002A" + "ignore" + "\\u002A";
    private static final String STR_SPLIT = "(\r\n|\n\r|\r|\n|" + IGNORE + ")";
    private static final String CLEAR_STR_PATTERN = "(\r\n|\n\r|\r|\n)";
    private static final String NBS_PATTERN = "[\\s\\u00A0]";

    public void assertTestCaseWSRequests(
            Project project,
            Map<String, Object> variables,
            WireMockAdmin wireMockAdmin,
            String testId,
            Step step
    ) throws Exception {
        if (wireMockAdmin == null) {
            return;
        }

        if (step.getExpectedServiceRequests().isEmpty()) {
            return;
        }

        if (step.getStepMode() == StepMode.REST_ASYNC) {
            pollWiremockUntilRequested(wireMockAdmin, project, step, variables, testId);
        }

        for (ExpectedServiceRequest request : step.getExpectedServiceRequests()) {
            checkExpectedServiceRequest(project, variables, wireMockAdmin, testId, request);
        }
    }

    private void pollWiremockUntilRequested(WireMockAdmin wireMockAdmin, Project project, Step step, Map<String, Object> variables, String testId) throws IOException {
        if (StringUtils.isEmpty(step.getMockPollingTimeout())) {
            return;
        }
        long timeout = AtProjectExecutor.parseLongOrVariable(variables, step.getMockPollingTimeout(), 0);
        if (timeout <= 0) {
            return;
        }
        long start = System.currentTimeMillis();
        DelayUtilities delayUtilities = new DelayUtilities();
        while (!checkRequestsCount(wireMockAdmin, project, step, variables, testId)) {
            delayUtilities.delay(1000);
            if ((System.currentTimeMillis() - start) > timeout) {
                throw new RuntimeException("Timeout for requests for stubs has expired");
            }
        }

        if (StringUtils.isEmpty(step.getMockRetryDelay())) {
            return;
        }
        delayUtilities.delay(AtProjectExecutor.parseLongOrVariable(variables, step.getMockRetryDelay(), 0));
    }

    private boolean checkRequestsCount(WireMockAdmin wireMockAdmin, Project project, Step step, Map<String, Object> variables, String testId) throws IOException {
        for (ExpectedServiceRequest request : step.getExpectedServiceRequests()) {
            long actual = wireMockAdmin.countRestRequests(createMockRequest(project, testId, request));
            long expected = AtProjectExecutor.parseLongOrVariable(variables, request.getCount(), 1);
            if (actual != expected) {
                return false;
            }
        }
        return true;
    }

    private void checkExpectedServiceRequest(
            Project project,
            Map<String, Object> variables,
            WireMockAdmin wireMockAdmin,
            String testId,
            ExpectedServiceRequest request)
            throws Exception {
        MockRequest mockRequest = createMockRequest(project, testId, request);
        List<WireMockRequest> actualRequests = wireMockAdmin.findRestRequests(mockRequest);
        long repeatCount = AtProjectExecutor.parseLongOrVariable(variables, request.getCount(), 1);
        if (actualRequests.size() != repeatCount) {
            throw new InvalidNumberOfMockRequests(repeatCount, actualRequests.size());
        }

        String expression = ExecutorUtils.insertSavedValues(request.getExpectedServiceRequest(), variables);
        String requestText = request.getNotEvalExprInBody() ? expression : ExecutorUtils.evaluateExpressions(expression, variables);
        Set<String> ignoredTags = isNotEmpty(request.getIgnoredTags()) ?
                Arrays.stream(request.getIgnoredTags().split(","))
                        .map(String::trim)
                        .collect(Collectors.toSet()) :
                Collections.emptySet();
        for (WireMockRequest actualRequest : actualRequests) {
            compareWSRequest(requestText, actualRequest, ignoredTags, request.getJsonCompareMode());

            // save variables from service request body
            this.saveVariablesFromServiceRequest(
                    actualRequest.getBody(), request.getScenarioVariablesFromServiceRequest(), variables);
        }
    }

    private MockRequest createMockRequest(Project project, String testId, ExpectedServiceRequest request) {
        MockRequest mockRequest = new MockRequest(project.getTestIdHeaderName(), testId);
        if (BooleanUtils.isTrue(request.getUrlPattern())) {
            mockRequest.setUrlPattern(request.getServiceName());
        } else {
            mockRequest.setUrl(request.getServiceName());
        }

        RequestMatcher matcher = RequestMatcher.build(request.getTypeMatching(), request.getPathFilter());
        if (matcher.isPresent()) {
            mockRequest.setBodyPatterns(Collections.singletonList(matcher));
        }
        return mockRequest;
    }

    /**
     * Compare expected and actual requests.
     */
    private void compareWSRequest(String expectedRequest, WireMockRequest actualRequest, Set<String> ignoredTags, String jsonCompareMode) throws ComparisonException {
        String contentType = actualRequest.getHeaders().get("Content-Type");

        if (contentType == null) {
            log.warn("No 'Content-Type' header use old method");
            this.compareWSRequestFallback(expectedRequest, actualRequest.getBody(), ignoredTags, jsonCompareMode);
        } else if (contentType.contains("application/xml") || contentType.contains("text/xml")) {
            compareWSRequestAsXml(expectedRequest, actualRequest.getBody(), ignoredTags);
        } else if (contentType.contains("application/json")) {
            if (expectedRequest == null) {
                return;
            }
            compareWSRequestAsJson(expectedRequest, actualRequest.getBody(), ignoredTags, jsonCompareMode);
        } else {
            compareWSRequestAsString(defaultString(expectedRequest), actualRequest.getBody());
        }
    }

    /**
     * Fallback behaviour, in case of 'Content-Type' header is not present.
     */
    private void compareWSRequestFallback(String expectedRequest, String actualRequest, Set<String> ignoredTags, String jsonCompareMode) throws ComparisonException {
        try {
            compareWSRequestAsXml(expectedRequest, actualRequest, ignoredTags);
        } catch (XMLUnitException uException) {
            // определяем, что упало при парсинге XML, пытаемся сравнить как JSON
            try {
                compareWSRequestAsJson(expectedRequest, actualRequest, ignoredTags, jsonCompareMode);
            } catch (JsonParsingException ex) {
                // определяем, что упало при парсинге JSON, далее сравниваем как строку
                if (uException.getCause() instanceof SAXParseException) {
                    compareWSRequestAsString(defaultString(expectedRequest), actualRequest);
                }
            }
        }
    }

    private void compareWSRequestAsXml(String expectedRequest, String actualRequest, Set<String> ignoredTags) throws ComparisonException {
        Diff diff = DiffBuilder.compare(defaultString(expectedRequest))
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

    private void compareWSRequestAsJson(String expected, String actual, Set<String> ignoringPaths, String mode) throws ComparisonException {
        if (StringUtils.isEmpty(expected) && StringUtils.isEmpty(actual)) {
            return;
        }
        ObjectMapper om = new ObjectMapper();
        try {
            om.readValue(expected, Map.class);
            om.readValue(actual, Map.class);
        } catch (Exception e) {
            throw new JsonParsingException(e);
        }

        try {
            List<Customization> customizations = ignoringPaths.stream()
                    .map(p -> new Customization(p, (o1, o2) -> true))
                    .collect(Collectors.toList());
            JSONAssert.assertEquals(
                    expected == null ? "" : expected.replaceAll(" ", " "),
                    actual.replaceAll(" ", " "),
                    new IgnoringComparator(
                            StringUtils.isEmpty(mode) ? JSONCompareMode.NON_EXTENSIBLE : JSONCompareMode.valueOf(mode),
                            customizations
                    )
            );
        } catch (Error assertionError) {
            throw new ComparisonException(assertionError.getMessage(), expected, actual);
        }
    }

    private void compareWSRequestAsString(String expectedRequest, String actualRequest) throws ComparisonException {
        String[] split = expectedRequest.split(STR_SPLIT);
        actualRequest = actualRequest.replaceAll(CLEAR_STR_PATTERN, "").replaceAll(NBS_PATTERN, " ");

        if (split.length == 1 && !Objects.equals(defaultIfNull(split[0], ""), defaultIfNull(actualRequest, ""))) {
            throw new ComparisonException("", expectedRequest, actualRequest);
        }

        int i = 0;
        boolean notEquals = false;
        StringBuilder diff = new StringBuilder("\n");
        for (String s : split) {
            i = actualRequest.indexOf(s.trim(), i);
            if (i < 0) {
                notEquals = true;
                diff.append(s.trim()).append("\n");
            }
        }

        if (notEquals) {
            throw new ComparisonException(diff.toString(), expectedRequest, actualRequest);
        }
    }

    private void saveVariablesFromServiceRequest(
            String request,
            List<ScenarioVariableFromServiceRequest> variablesFromServiceRequest,
            Map<String, Object> variables) {
        for (ScenarioVariableFromServiceRequest scenarioVariable : variablesFromServiceRequest) {
            String savedValue = this.getVariableFromServiceRequest(request, scenarioVariable);
            variables.put(scenarioVariable.getScenarioVariableName(), savedValue);
        }
    }

    private String getVariableFromServiceRequest(String request, ScenarioVariableFromServiceRequest variable) {
        switch (variable.getMatchingType()) {
            case REGEXP:
                return RegexUtils.getValueByRegex(request, variable.getExpression());
            case XPATH:
                try {
                    return XMLUtils.getValueByXPath(request, variable.getExpression());
                } catch (Exception e) {
                    log.error("Unable to read value from XML by XPath", e);
                    return EMPTY;
                }
            case JSONPATH:
                // Same as default
            default:
                return JsonPath.read(request, variable.getExpression());
        }
    }
}
