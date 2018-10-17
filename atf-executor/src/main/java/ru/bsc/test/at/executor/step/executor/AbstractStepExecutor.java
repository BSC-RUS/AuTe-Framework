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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.PathNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.*;
import ru.bsc.test.at.executor.helper.NamedParameterStatement;
import ru.bsc.test.at.executor.helper.client.api.ClientResponse;
import ru.bsc.test.at.executor.helper.client.impl.mq.ClientMQRequest;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.at.executor.step.executor.scriptengine.JSScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngineFunctionResult;
import ru.bsc.test.at.executor.validation.IgnoringComparator;
import ru.bsc.test.at.executor.validation.MaskComparator;

import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public abstract class AbstractStepExecutor implements IStepExecutor {

    private final static int POLLING_RETRY_TIMEOUT_MS = 1000;
    private static final String DEFAULT_CONTENT_TYPE = "text/xml";

    void sendMessagesToQuery(Project project, Step step, Map<String, Object> scenarioVariables, MqClient mqClient, String testIdHeaderName, String testId) throws Exception {
        log.debug("Send MQ messages to query {} {} {}", project, step, scenarioVariables);
        if (step.getMqMessages() == null) {
            log.warn("Message list is empty");
            return;
        }
        Map<String, Object> generatedProperties = new HashMap<>();
        for (MqMessage message : step.getMqMessages()) {
            if (message.isEmpty()) {
                continue;
            }
            String messageText = insertSavedValues(message.getMessage(), scenarioVariables);
            generatedProperties.clear();
            message.getProperties().forEach(p -> {
                NameValueProperty property = new NameValueProperty();
                property.setName(p.getName());
                try {
                    String template = insertSavedValues(p.getValue(), scenarioVariables);
                    String value = evaluateExpressions(template, scenarioVariables, null);
                    generatedProperties.put(p.getName(), value);
                } catch (ScriptException e) {
                    log.error("Error while evaluate expression", e);
                }
            });
            ClientMQRequest clientMQRequest = new ClientMQRequest(message.getQueueName(), messageText, generatedProperties, testId, testIdHeaderName);
            mqClient.request(clientMQRequest);
        }
    }

    void parseMockRequests(Project project, Step step, WireMockAdmin wireMockAdmin, Map<String, Object> scenarioVariables, String testId) throws IOException, XPathExpressionException, ParserConfigurationException, SAXException {
        log.debug("Parse mock requests {} {} {} {}", project, step, scenarioVariables, testId);
        if (step.getParseMockRequestUrl() != null) {
            MockRequest mockRequest = new MockRequest();
            mockRequest.getHeaders().put(project.getTestIdHeaderName(), createEqualsToHeader(testId));
            mockRequest.setUrl(step.getParseMockRequestUrl());
            RequestList list = wireMockAdmin.findRestRequests(mockRequest);
            if (list.getRequests() != null && !list.getRequests().isEmpty()) {

                // Parse request
                WireMockRequest request = list.getRequests().get(0);

                DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = builderFactory.newDocumentBuilder();
                Document xmlDocument = builder.parse(new InputSource(new StringReader(request.getBody())));
                XPath xPath = XPathFactory.newInstance().newXPath();
                String valueFromMock = xPath.compile(step.getParseMockRequestXPath()).evaluate(xmlDocument);

                scenarioVariables.put(step.getParseMockRequestScenarioVariable().trim(), valueFromMock);
            }
        }
    }

    private void jsonComparing(String expectedResponse, String responseContent, String jsonCompareMode) throws Exception {
        log.debug("Json comparing {} {} {}", expectedResponse, responseContent, jsonCompareMode);
        if ((StringUtils.isNotEmpty(expectedResponse) || StringUtils.isNotEmpty(responseContent)) &&
                (!responseContent.equals(expectedResponse))) {
            try {
                JSONAssert.assertEquals(
                        expectedResponse == null ? "" : expectedResponse.replaceAll(" ", " "),
                        responseContent.replaceAll(" ", " "), // Fix broken space in response
                        new IgnoringComparator(StringUtils.isEmpty(jsonCompareMode) ? JSONCompareMode.NON_EXTENSIBLE : JSONCompareMode.valueOf(jsonCompareMode))
                );
            } catch (Error assertionError) {
                throw new Exception(assertionError);
            }
        }
    }

    void saveValuesByJsonXPath(Step step, String responseContent, Map<String, Object> scenarioVariables) {
        log.debug("Save values by json xpath {} {} {}", step, responseContent, scenarioVariables);
        if (isNotEmpty(responseContent) && isNotEmpty(step.getJsonXPath())) {
            String[] lines = step.getJsonXPath().split("\\r?\\n");
            for (String line : lines) {
                String[] lineParts = line.split("=", 2);
                String parameterName = lineParts[0].trim();
                String jsonXPath = lineParts[1];
                scenarioVariables.put(parameterName, JsonPath.read(responseContent, jsonXPath).toString());
            }
        }
    }

    void setMockResponses(WireMockAdmin wireMockAdmin, Project project, String testId, List<MockServiceResponse> responseList) throws IOException {
        log.debug("Setting REST-mock responses {} {} {} {}", wireMockAdmin, project, testId, responseList);
        Long priority = 0L;
        if (responseList != null && wireMockAdmin != null) {
            for (MockServiceResponse mockServiceResponse : responseList) {
                MockDefinition mockDefinition = new MockDefinition(priority--, project.getTestIdHeaderName(), testId);
                mockDefinition.getRequest().setUrl(mockServiceResponse.getServiceUrl());
                mockDefinition.getRequest().setMethod(mockServiceResponse.getHttpMethodOrDefault());
                if(isNotEmpty(mockServiceResponse.getPassword()) || isNotEmpty(mockServiceResponse.getUserName())){
                    BasicAuthCredentials credentials = new BasicAuthCredentials();
                    credentials.setPassword(mockServiceResponse.getPassword());
                    credentials.setUsername(mockServiceResponse.getUserName());
                    mockDefinition.getRequest().setBasicAuthCredentials(credentials);
                }
                if(isNotEmpty(mockServiceResponse.getPathFilter())) {
                    MatchesXPath matchesXPath = new MatchesXPath();
                    matchesXPath.setMatchesXPath(mockServiceResponse.getPathFilter());
                    mockDefinition.getRequest().setBodyPatterns(Collections.singletonList(matchesXPath));
                }

                mockDefinition.getResponse().setBody(mockServiceResponse.getResponseBody());
                mockDefinition.getResponse().setStatus(mockServiceResponse.getHttpStatus());

                HashMap<String, String> headers = new HashMap<>();
                if(mockServiceResponse.getHeaders() != null) {
                    mockServiceResponse.getHeaders().forEach(header -> {
                        headers.put(header.getHeaderName(), header.getHeaderValue());
                    });
                }
                mockDefinition.getResponse().setHeaders(headers);
                String contentType = StringUtils.isNoneBlank(mockServiceResponse.getContentType()) ?
                        mockServiceResponse.getContentType() :
                        DEFAULT_CONTENT_TYPE;
                mockDefinition.getResponse().getHeaders().put("Content-Type", contentType);

                wireMockAdmin.addRestMapping(mockDefinition);
            }
        }
    }

    boolean tryUsePolling(Step step, ClientResponse clientResponse) throws InterruptedException {
        Assert.notNull(clientResponse, "client response must not be null");
        String content = clientResponse.getContent();
        log.debug("trying use polling {} {}", step, content);
        if (!step.getUsePolling()) {
            return false;
        }
        boolean retry = true;
        try {
            if (StringUtils.isNotEmpty(content) && JsonPath.read(content, step.getPollingJsonXPath()) != null) {
	            log.info("Required attribute for polling found in path {}. Stop polling", step.getPollingJsonXPath());
	            retry = false;
            }
        } catch (PathNotFoundException | IllegalArgumentException e) {
            log.info("Required attribute for polling not found in path {}. Continue polling", step.getPollingJsonXPath());
            retry = true;
        }
        if (retry) {
            Thread.sleep(POLLING_RETRY_TIMEOUT_MS);
        }
        log.debug("trying use polling? Is - {}", retry);
        return retry;
    }

    void executeSql(Connection connection, Step step, Map<String, Object> scenarioVariables, StepResult stepResult) throws SQLException, ScriptException {
        log.debug("executing sql {}, {}", step, scenarioVariables);
        if (!step.getSqlDataList().isEmpty() && connection != null) {
            List<String> queryList = new LinkedList<>();
            stepResult.setSqlQueryList(queryList);
            for (SqlData sqlData : step.getSqlDataList()) {
                if (StringUtils.isNotEmpty(sqlData.getSql()) && StringUtils.isNotEmpty(sqlData.getSqlSavedParameter())) {
                    String query = evaluateExpressions(sqlData.getSql(), scenarioVariables, null);
                    queryList.add(query);
                    try (NamedParameterStatement statement = new NamedParameterStatement(connection, query)) {
                        SqlResultType sqlResultType = sqlData.getSqlReturnType();
                        // Вставить в запрос параметры из scenarioVariables, если они есть.
                        for (Map.Entry<String, Object> scenarioVariable : scenarioVariables.entrySet()) {
                            statement.setString(scenarioVariable.getKey(), String.valueOf(scenarioVariable.getValue()));
                        }
                        try (ResultSet rs = statement.executeQuery()) {
                            log.debug("Executing query {}", sqlData);
                            if (sqlResultType == SqlResultType.ROW) {
                                int columnCount = rs.getMetaData().getColumnCount();
                                if (rs.next()) {
                                    String[] sqlSavedParameterList = sqlData.getSqlSavedParameter().split(",");
                                    int i = 1;
                                    for (String parameterName: sqlSavedParameterList) {
                                        if (parameterName.trim().isEmpty()) {
                                            continue;
                                        }
                                        if (i > columnCount) {
                                            break;
                                        }
                                        scenarioVariables.put(parameterName.trim(), rs.getString(i));
                                        i++;
                                    }
                                }
                            } else if (sqlResultType == SqlResultType.OBJECT) {
                                log.debug("Reading Object from result set ...");
                                Object result = rs.next() ? rs.getObject(1) : null;
                                scenarioVariables.put(sqlData.getSqlSavedParameter().trim(), result);
                            } else if (sqlResultType == SqlResultType.LIST) {
                                log.debug("Reading List from result set ...");
                                List<Object> columnData = new ArrayList<>();
                                while (rs.next()) {
                                    columnData.add(rs.getObject(1));
                                }
                                scenarioVariables.put(sqlData.getSqlSavedParameter().trim(), columnData);
                            } else {
                                log.debug("Reading custom result set ...");
                                List<String> columnNameList = new LinkedList<>();
                                for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
                                    columnNameList.add(rs.getMetaData().getColumnName(i));
                                }
                                List<Map<String, Object>> resultData = new ArrayList<>();
                                while (rs.next()) {
                                    Map<String, Object> values = new HashMap<>();
                                    for (String columnName : columnNameList) {
                                        values.put(columnName, rs.getObject(columnName));
                                    }
                                    resultData.add(values);
                                }
                                scenarioVariables.put(sqlData.getSqlSavedParameter().trim(), resultData);
                            }
                        }
                    }
                }
            }
        }
    }

    public static String insertSavedValues(String template, Map<String, Object> scenarioVariables) {
        log.debug("insert saved values {}, {}", template, scenarioVariables);
        if (template != null) {
            for (Map.Entry<String, Object> value : scenarioVariables.entrySet()) {
                String key = String.format("%%%s%%", value.getKey());
                template = template.replaceAll(key, Matcher.quoteReplacement(value.getValue() == null ? "" : String.valueOf(value.getValue())));
            }
        }
        log.debug("insert saved values result {}", template);
        return template;
    }

    String insertSavedValuesToURL(String template, Map<String, Object> scenarioVariables) throws UnsupportedEncodingException {
        log.debug("insert saved values to URL {}, {}", template, scenarioVariables);
        if (template != null) {
            for (Map.Entry<String, Object> value : scenarioVariables.entrySet()) {
                String key = String.format("%%%s%%", value.getKey());
                template = template.replaceAll(key, Matcher.quoteReplacement(URLEncoder.encode(value.getValue() == null ? "" : String.valueOf(value.getValue()), "UTF-8")));
            }
        }
        log.debug("insert saved values to URL result {}", template);
        return template;
    }

    public static String evaluateExpressions(String template, Map<String, Object> scenarioVariables, ClientResponse responseData) throws ScriptException {
        log.debug("evaluate expressions {}, {} {}", template, scenarioVariables, responseData);
        String result = template;
        if (result != null) {
            Pattern p = Pattern.compile(".*?<f>(.+?)</f>.*?", Pattern.MULTILINE);
            Matcher m = p.matcher(result);
            while (m.find()) {
                log.debug("regexp matches {}", p.pattern());
                ScriptEngine engine = new JSScriptEngine();
                ScriptEngineFunctionResult evalResult = engine.executeFunction(m.group(1), scenarioVariables);
                result = result.replace(
                        "<f>" + m.group(1) + "</f>",
                        Matcher.quoteReplacement(evalResult.getResult())
                );
                log.debug("evaluating result {}", result);
            }
        }
        log.debug("evaluate expressions result {}", responseData);
        return result;
    }

    int calculateNumberRepetitions(Step step, Map<String, Object> scenarioVariables) {
        int numberRepetitions;
        try {
            numberRepetitions = Integer.parseInt(step.getNumberRepetitions());
        } catch (NumberFormatException e) {
            try {
                numberRepetitions = Integer.parseInt(String.valueOf(scenarioVariables.get(step.getNumberRepetitions())));
            } catch (NumberFormatException ex) {
                numberRepetitions = 1;
            }
        }
        numberRepetitions = numberRepetitions > 300 ? 300 : numberRepetitions;
        return numberRepetitions;
    }

    void checkScenarioVariables(Step step, Map<String, Object> scenarioVariables) throws Exception {
        if (step.getSavedValuesCheck() != null) {
            for (Map.Entry<String, String> entry : step.getSavedValuesCheck().entrySet()) {
                String valueExpected = entry.getValue() == null ? "" : entry.getValue();
                for (Map.Entry<String, Object> savedVal : scenarioVariables.entrySet()) {
                    String key = String.format("%%%s%%", savedVal.getKey());
                    valueExpected = valueExpected.replaceAll(key, String.valueOf(savedVal.getValue()));
                }
                String valueActual = String.valueOf(scenarioVariables.get(entry.getKey()));
                if (!valueExpected.equals(valueActual)) {
                    throw new Exception("Saved value " + entry.getKey() + " = " + valueActual + ". Expected: " + valueExpected);
                }
            }
        }
    }

    void checkResponseBody(Step step, String expectedResponse, String actualResponse) throws Exception {
        if (!step.getExpectedResponseIgnore()) {
            if (step.getResponseCompareMode() == null) {
                jsonComparing(expectedResponse, actualResponse, step.getJsonCompareMode());
            } else {
                switch (step.getResponseCompareMode()) {
                    case FULL_MATCH:
                        if (!StringUtils.equals(expectedResponse, actualResponse)) {
                            throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + actualResponse);
                        }
                        break;
                    case IGNORE_MASK:
                        if (!MaskComparator.compare(expectedResponse, actualResponse)) {
                            throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + actualResponse);
                        }
                        break;
                    default:
                        jsonComparing(expectedResponse, actualResponse, step.getJsonCompareMode());
                        break;
                }
            }
        }
    }

    void compareResponse(Step step, String expectedResponse, ClientResponse responseData) throws Exception {
        if (step.getExpectedResponseIgnore()) {
            return;
        }

        if (step.getResponseCompareMode() == null) {
            jsonComparing(expectedResponse, responseData, step.getJsonCompareMode());
        } else {
            log.debug("Response compare mode {}, ", step.getResponseCompareMode());
            switch (step.getResponseCompareMode()) {
                case FULL_MATCH:
                    if (!StringUtils.equals(expectedResponse, responseData.getContent())) {
                        throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + responseData.getContent());
                    }
                    break;
                case IGNORE_MASK:
                    if (!MaskComparator.compare(expectedResponse, responseData.getContent())) {
                        throw new Exception("\nExpected value: " + expectedResponse + ".\nActual value: " + responseData.getContent());
                    }
                    break;
                default:
                    jsonComparing(expectedResponse, responseData, step.getJsonCompareMode());
                    break;
            }
        }
    }

    private void jsonComparing(String expectedResponse, ClientResponse responseData, String jsonCompareMode) throws Exception {
        if ((isNotEmpty(expectedResponse) || isNotEmpty(responseData.getContent())) &&
                (!responseData.getContent().equals(expectedResponse))) {
            try {
                JSONAssert.assertEquals(
                        expectedResponse == null ? "" : expectedResponse.replaceAll(" ", " "),
                        // Fix broken space in response
                        responseData.getContent().replaceAll(" ", " "),
                        new IgnoringComparator(StringUtils.isEmpty(jsonCompareMode) ?
                                JSONCompareMode.NON_EXTENSIBLE :
                                JSONCompareMode.valueOf(jsonCompareMode))
                );
            } catch (Error assertionError) {
                throw new Exception(assertionError);
            }
        }
    }

    void setMqMockResponses(WireMockAdmin wireMockAdmin, String testId, List<MqMock> mqMockList, Map<String, Object> scenarioVariables) throws Exception {
        if (mqMockList != null && mqMockList.size() > 0) {
            log.debug("Setting MQ mock responses {} {} {} {}", wireMockAdmin, testId, mqMockList, scenarioVariables);
            if (wireMockAdmin == null) {
                throw new Exception("wireMockAdmin is not configured in env.yml");
            }
            for (MqMock mqMock : mqMockList) {
                MqMockDefinition mockMessage = new MqMockDefinition();
                mockMessage.setSourceQueueName(mqMock.getSourceQueueName());
                mockMessage.setHttpUrl(mqMock.getHttpUrl());
                mockMessage.setTestId(testId);

                for (MqMockResponse part : mqMock.getResponses()) {
                    MqMockDefinitionResponse definitionPart = new MqMockDefinitionResponse(
                            evaluateExpressions(part.getResponseBody(), scenarioVariables, null),
                            part.getDestinationQueueName()
                    );
                    mockMessage.getResponses().add(definitionPart);
                }
                wireMockAdmin.addMqMapping(mockMessage);
            }
        }
    }

    private HashMap<String, String> createEqualsToHeader(String testId) {
        HashMap<String, String> headers = new HashMap<>();
        headers.put("equalTo", testId);
        return headers;
    }
}
