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
import lombok.AllArgsConstructor;
import lombok.Getter;
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
import ru.bsc.test.at.executor.exception.IncorrectRequestsOrderException;
import ru.bsc.test.at.executor.exception.InvalidNumberOfMockRequests;
import ru.bsc.test.at.executor.exception.JsonParsingException;
import ru.bsc.test.at.executor.exception.UnMockedRequestsException;
import ru.bsc.test.at.executor.model.ExpectedServiceRequest;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.ScenarioVariableFromServiceRequest;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepMode;
import ru.bsc.test.at.executor.service.AtProjectExecutor;
import ru.bsc.test.at.executor.service.DelayUtilities;
import ru.bsc.test.at.executor.step.executor.ExecutorUtils;
import ru.bsc.test.at.executor.validation.IgnoringComparator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static org.apache.commons.lang3.StringUtils.defaultString;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * Created by sdoroshin on 30.05.2017.
 */
@Slf4j
public class ServiceRequestsComparatorHelper {

    private static final String REGEX_PART = "\\u002A" + "part" + "\\u002A";
    private static final String STRING_PART = "*part*";

    public void assertTestCaseWSRequests(Project project, Map<String, Object> variables, WireMockAdmin wireMockAdmin, String testId, Step step) throws Exception {
        if (wireMockAdmin == null) {
            return;
        }

        if (step.getExpectedServiceRequests().isEmpty()) {
            List<WireMockRequest> actualRequestsByTestIds = wireMockAdmin.findRestRequests(getMockRequest(project, testId));
            LinkedHashMap<ExpectedServiceRequest, List<WireMockRequest>> expectedServiceRequestListMap = getOrderedRequestsByExpectedMap(wireMockAdmin, project, step, testId, variables);
            List<WireMockRequest> actualRequests = getRequestsByExpected(expectedServiceRequestListMap);
            checkUnMockedRequest(step, actualRequestsByTestIds, actualRequests);
            checkRequestsOrder(step, actualRequestsByTestIds, actualRequests);
            return;
        }

        if (step.getStepMode() == StepMode.REST_ASYNC) {
            pollWiremockUntilRequested(wireMockAdmin, project, step, variables, testId);
        }

        checkExpectedServiceRequests(project, variables, wireMockAdmin, testId, step);
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
            long actual = wireMockAdmin.countRestRequests(createMockRequest(project, testId, request, variables));
            long expected = AtProjectExecutor.parseLongOrVariable(variables, request.getCount(), 1);
            if (actual != expected) {
                return false;
            }
        }
        return true;
    }

    private void checkExpectedServiceRequests(
            Project project,
            Map<String, Object> variables,
            WireMockAdmin wireMockAdmin,
            String testId,
            Step step)
            throws Exception {

        List<WireMockRequest> actualRequestsByTestIds = wireMockAdmin.findRestRequests(getMockRequest(project, testId));
        LinkedHashMap<ExpectedServiceRequest, List<WireMockRequest>> expectedServiceRequestListMap = getOrderedRequestsByExpectedMap(wireMockAdmin, project, step, testId, variables);
        List<WireMockRequest> actualRequests = getRequestsByExpected(expectedServiceRequestListMap);
        checkUnMockedRequest(step, actualRequestsByTestIds, actualRequests);
        checkRequestsOrder(step, actualRequestsByTestIds, actualRequests);

        List<ExpectedServiceRequest> requestList = step.getExpectedServiceRequests();
        requestList.forEach(request -> {
            List<WireMockRequest> requests = expectedServiceRequestListMap.get(request);
            long repeatCount = AtProjectExecutor.parseLongOrVariable(variables, request.getCount(), 1);
            if (requests.size() != repeatCount) {
                throw new InvalidNumberOfMockRequests(repeatCount, requests.size(), request.getServiceName());
            }

            String expression = ExecutorUtils.insertSavedValues(request.getExpectedServiceRequest(), variables);
            String requestText =
                    request.getNotEvalExprInBody() ? expression : ExecutorUtils.evaluateExpressions(expression, variables);
            Set<String> ignoredTags = isNotEmpty(request.getIgnoredTags()) ?
                    Arrays.stream(request.getIgnoredTags().split(","))
                            .map(String::trim)
                            .collect(Collectors.toSet()) :
                    Collections.emptySet();
            for (WireMockRequest actualRequest : requests) {
                compareWSRequest(requestText, actualRequest, ignoredTags, request.getJsonCompareMode());

                // save variables from service request body
                this.saveVariablesFromServiceRequest(
                        actualRequest.getBody(), request.getScenarioVariablesFromServiceRequest(), variables);
            }
        });

    }

    private MockRequest getMockRequest(Project project, String testId) {
        MockRequest requestByTestId = new MockRequest(project.getTestIdHeaderName(), testId);
        requestByTestId.setUrlPattern("^(?!\\/mq_).*");
        return requestByTestId;
    }

    /**
     * проверяем порядок запросов
     */
    private void checkRequestsOrder(Step step, List<WireMockRequest> actualRequestsByTestIds, List<WireMockRequest> actualRequests) {
        if (Boolean.TRUE.equals(step.getCheckRequestsOrder())) {
            actualRequestsByTestIds.sort(Comparator.comparing(WireMockRequest::getLoggedDate));
            if (!actualRequestsByTestIds.equals(actualRequests)) {
                throw new IncorrectRequestsOrderException();
            }
        }
    }


    private LinkedHashMap<ExpectedServiceRequest, List<WireMockRequest>> getOrderedRequestsByExpectedMap(WireMockAdmin wireMockAdmin, Project project, Step step, String testId, Map<String, Object> variables) {
        return step.getExpectedServiceRequests().stream().collect(Collectors.toMap(identity(), request -> {
            try {
                return wireMockAdmin.findRestRequests(createMockRequest(project, testId, request, variables));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, (a, b) -> a, LinkedHashMap::new));
    }

    private List<WireMockRequest> getRequestsByExpected(LinkedHashMap<ExpectedServiceRequest, List<WireMockRequest>> actualRequestsByExpected) {
        return actualRequestsByExpected.values().stream()
            .peek(l -> l.sort(Comparator.comparing(WireMockRequest::getLoggedDate)))
            .flatMap(List::stream)
            .collect(Collectors.toList());
    }

    /**
     * получаем список запросов из wiremock для которых нет заглушек
     */
    private void checkUnMockedRequest(Step step, List<WireMockRequest> actualRequestsByTestIds, List<WireMockRequest> actualRequestsOrderedList) {
        List<WireMockRequest> unMockedRequests = actualRequestsByTestIds.stream().
                filter(r -> !actualRequestsOrderedList.contains(r)).collect(Collectors.toList());

        if (!Boolean.TRUE.equals(step.getIgnoreRequestsInvocations()) && !unMockedRequests.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Sent request without mock : \n");
            unMockedRequests.forEach(r -> sb.append(r.getUrl()).append(" ").append(r.getBody()).append("\n"));
            throw new UnMockedRequestsException(sb.toString());
        }
    }

    private MockRequest createMockRequest(Project project, String testId, ExpectedServiceRequest request, Map<String, Object> variables) {
        MockRequest mockRequest = new MockRequest(project.getTestIdHeaderName(), testId);
        final String url = ExecutorUtils.insertSavedValues(request.getServiceName(), variables);
        if (BooleanUtils.isTrue(request.getUrlPattern())) {
            mockRequest.setUrlPattern(url);
        } else {
            mockRequest.setUrl(url);
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
        Map.Entry<String, String> any =
                actualRequest.getHeaders().entrySet().stream().filter(e -> "Content-Type".equalsIgnoreCase(e.getKey()))
                        .findAny().orElse(null);
        String contentType = any != null ? any.getValue() : null;
        boolean isMultipart = contentType != null && contentType.contains("multipart/form-data");
        String actualRequestBody = actualRequest.getBody();

        List<Request> requests;
        if (isMultipart && expectedRequest.contains(STRING_PART)) { // если мы хотим сравнивать части отдельно
            requests = splitMultipart(expectedRequest, contentType, actualRequestBody);
        } else {
            requests = Collections.singletonList(new Request(contentType, expectedRequest, actualRequestBody));
        }

        requests.forEach(request -> compareWSRequest(request, ignoredTags, jsonCompareMode));

    }

    private List<Request> splitMultipart(
            String expectedRequest,
            String contentType,
            String actualRequestBody
    ) {
        List<Request> requests;
        String[] expectedParts = expectedRequest.split(REGEX_PART);
        int indStart = contentType.indexOf("boundary") + "boundary".length() + 1;
        int indEnd = contentType.indexOf(";", indStart);
        indEnd = indEnd == -1 ? contentType.length() : indEnd;
        String boundary = contentType.substring(indStart, indEnd);
        String[] actualParts = actualRequestBody.split("--" + boundary);
        requests = new ArrayList<>();
        if (actualParts.length != expectedParts.length) {
            throw new ComparisonException("Different number of part", expectedRequest, actualRequestBody);
        }
        for (int i = 1; i < actualParts.length - 1; i++) {
            String part = actualParts[i];
            // TODO null указан специально, т.к. микросервис вызывает метод с неправильными хедерами, в этом слуае вызовется compareWSRequestFallback
            requests.add(new Request(null, expectedParts[i], part));
        }
        return requests;
    }

    private void compareWSRequest(Request request, Set<String> ignoredTags, String jsonCompareMode) throws ComparisonException {

        String contentType = request.getContentType();
        String expectedRequest = request.getExpectedBody();
        String actualRequestBody = request.getActualBody();

        if (contentType == null) {
            log.warn("No 'Content-Type' header use old method");
            this.compareWSRequestFallback(expectedRequest, actualRequestBody, ignoredTags, jsonCompareMode);
        } else if (contentType.contains("application/xml") || contentType.contains("text/xml")) {
            compareWSRequestAsXml(expectedRequest, actualRequestBody, ignoredTags);
        } else if (contentType.contains("application/json")) {
            if (expectedRequest == null) {
                return;
            }
            compareWSRequestAsJson(expectedRequest, actualRequestBody, ignoredTags, jsonCompareMode);
        } else {
            compareWSRequestAsString(defaultString(expectedRequest), actualRequestBody);
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
        ComparisonResult comparisonResult = CompareUtils.compareRequestAsXml(expectedRequest, actualRequest, ignoredTags);

        if (comparisonResult.isHasDifferences()) {
            throw new ComparisonException(comparisonResult.getDiff(), expectedRequest, actualRequest);
        }
    }

    private void compareWSRequestAsJson(String expected, String actual, Set<String> ignoringPaths, String mode) throws ComparisonException {
        if (StringUtils.isEmpty(expected) && StringUtils.isEmpty(actual)) {
            return;
        }
        ComparisonResult comparisonResult = CompareUtils.compareRequestAsJson(expected, actual, ignoringPaths, mode);

        if (comparisonResult.isHasDifferences()) {
            throw new ComparisonException(comparisonResult.getDiff(), expected, actual);
        }
    }

    private void compareWSRequestAsString(String expectedRequest, String actualRequest) throws ComparisonException {
        ComparisonResult comparisonResult = CompareUtils.compareRequestAsString(expectedRequest, actualRequest);
        if (comparisonResult.isHasDifferences()) {
            throw new ComparisonException(comparisonResult.getDiff(), expectedRequest, actualRequest);
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

    @Getter
    @AllArgsConstructor
    private static class Request {
        private String contentType;
        private String expectedBody;
        private String actualBody;
    }
}
