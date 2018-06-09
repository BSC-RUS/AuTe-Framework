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

import org.xml.sax.SAXParseException;
import org.xmlunit.XMLUnitException;
import org.xmlunit.builder.DiffBuilder;
import org.xmlunit.diff.Diff;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.MockRequest;
import ru.bsc.test.at.executor.ei.wiremock.model.WireMockRequest;
import ru.bsc.test.at.executor.exception.ComparisonException;
import ru.bsc.test.at.executor.model.ExpectedServiceRequest;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.service.AtProjectExecutor;

import java.util.*;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ru.bsc.test.at.executor.step.executor.AbstractStepExecutor.evaluateExpressions;
import static ru.bsc.test.at.executor.step.executor.AbstractStepExecutor.insertSavedValues;

/**
 * Created by sdoroshin on 30.05.2017.
 *
 */
public class ServiceRequestsComparatorHelper {

    private static final String IGNORE = "\\u002A" + "ignore" + "\\u002A";
    private static final String CLEAR_STR_PATTERN = "(\r\n|\n\r|\r|\n)";
    private static final String NBS_PATTERN = "[\\s\\u00A0]";


    private void compareWSRequest(String expectedRequest, String actualRequest, Set<String> ignoredTags) throws ComparisonException {
        try{
            compareWSRequestAsXml(expectedRequest, actualRequest, ignoredTags);
        }catch (XMLUnitException uException){
            // определяем, что упало при парсинге XML, далее сравниваем как строку
            if(uException.getCause() instanceof SAXParseException){
                compareWSRequestAsString(expectedRequest, actualRequest);
            }
        }
    }

    private void compareWSRequestAsXml(String expectedRequest, String actualRequest, Set<String> ignoredTags) throws ComparisonException {
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

    private void compareWSRequestAsString(String expectedRequest, String actualRequest) throws ComparisonException {
        String[] split = expectedRequest.replaceAll(CLEAR_STR_PATTERN, "").replaceAll(NBS_PATTERN, " ").split(IGNORE);
        actualRequest = actualRequest.replaceAll(CLEAR_STR_PATTERN, "").replaceAll(NBS_PATTERN, " ");

        if(split.length == 1 && !Objects.equals(defaultIfNull(split[0],""), defaultIfNull(actualRequest,""))){
            throw new ComparisonException(null, expectedRequest, actualRequest);
        }

        int i = 0;
        boolean notEquals = false;
        for (String s : split){
            i = actualRequest.indexOf(s.trim(), i);
            if (i < 0) {
                notEquals = true;
                break;
            }
        }

        if (notEquals) {
            throw new ComparisonException(null, expectedRequest, actualRequest);
        }

    }

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

        for (ExpectedServiceRequest request : step.getExpectedServiceRequests()) {
            checkExpectedServiceRequest(project, variables, wireMockAdmin, testId, request);
        }
    }

    private void checkExpectedServiceRequest(
            Project project,
            Map<String, Object> variables,
            WireMockAdmin wireMockAdmin,
            String testId,
            ExpectedServiceRequest request
    ) throws Exception {
        MockRequest mockRequest = new MockRequest();
        mockRequest.getHeaders().put(project.getTestIdHeaderName(), createEqualToHeader(testId));
        mockRequest.setUrl(request.getServiceName());
        List<WireMockRequest> actualRequests = wireMockAdmin.findRestRequests(mockRequest).getRequests();
        if (actualRequests == null) {
            actualRequests = new ArrayList<>();
        }

        long repeatCount = AtProjectExecutor.parseLongOrVariable(variables, request.getCount(), 1);
        if (actualRequests.size() != repeatCount) {
            throw new Exception(String.format(
                    "Invalid number of service requests: expected: %d, actual: %d",
                    repeatCount,
                    actualRequests.size()
            ));
        }

        String requestExpression = insertSavedValues(request.getExpectedServiceRequest(), variables);
        String requestText = evaluateExpressions(requestExpression, variables, null);
        Set<String> ignoredTags = isNotEmpty(request.getIgnoredTags()) ?
                                  Arrays.stream(request.getIgnoredTags().split(","))
                                          .map(String::trim)
                                          .collect(Collectors.toSet()) :
                                  null;
        for (WireMockRequest actualRequest : actualRequests) {
            compareWSRequest(requestText, actualRequest.getBody(), ignoredTags);
        }
    }

    private Map<String, String> createEqualToHeader(String testId) {
        Map<String, String> header = new HashMap<>();
        header.put("equalTo", testId);
        return header;
    }
}
