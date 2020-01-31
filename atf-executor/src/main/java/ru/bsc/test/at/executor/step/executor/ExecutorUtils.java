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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.executor.ei.wiremock.WireMockAdmin;
import ru.bsc.test.at.executor.ei.wiremock.model.BasicAuthCredentials;
import ru.bsc.test.at.executor.ei.wiremock.model.MockDefinition;
import ru.bsc.test.at.executor.ei.wiremock.model.MqMockDefinition;
import ru.bsc.test.at.executor.ei.wiremock.model.MqMockDefinitionResponse;
import ru.bsc.test.at.executor.ei.wiremock.model.RequestMatcher;
import ru.bsc.test.at.executor.helper.NamedParameterStatement;
import ru.bsc.test.at.executor.helper.client.impl.mq.ClientMQRequest;
import ru.bsc.test.at.executor.helper.client.impl.mq.MqClient;
import ru.bsc.test.at.executor.model.MockServiceResponse;
import ru.bsc.test.at.executor.model.MqMessage;
import ru.bsc.test.at.executor.model.MqMock;
import ru.bsc.test.at.executor.model.MqMockResponse;
import ru.bsc.test.at.executor.model.NameValueProperty;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.SqlData;
import ru.bsc.test.at.executor.model.SqlResultType;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.at.executor.step.executor.scriptengine.JSScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngine;
import ru.bsc.test.at.executor.step.executor.scriptengine.ScriptEngineFunctionResult;
import ru.bsc.test.at.util.MultipartConstant;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * @author mobrubov
 * created on 29.12.2018 12:22
 */
@Slf4j
public class ExecutorUtils {

    private static final String DEFAULT_CONTENT_TYPE = "text/xml";

    private ExecutorUtils() {
        super();
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

    public static String evaluateExpressions(String template, Map<String, Object> scenarioVariables) {
        log.debug("evaluate expressions {}, {}", template, scenarioVariables);
        String result = template;
        if (result != null && !Base64.isBase64(result)) {
            String[] foundExpressions = StringUtils.substringsBetween(result,"<f>", "</f>");
            if (foundExpressions != null) {
                for (String expression : foundExpressions) {
                    ScriptEngine engine = new JSScriptEngine();
                    ScriptEngineFunctionResult evalResult = engine.executeFunction(expression, scenarioVariables);
                    result = result.replace(
                            "<f>" + expression + "</f>",
                            Matcher.quoteReplacement(evalResult.getResult())
                    );
                    log.debug("evaluating result {}", result);
                }
            }
        }
        log.debug("evaluate expressions result {}", result);
        return result;
    }

    static void sendMessagesToQuery(Project project, Step step, Map<String, Object> scenarioVariables, MqClient mqClient, String testIdHeaderName, String testId) throws Exception {
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
                String template = insertSavedValues(p.getValue(), scenarioVariables);
                String value = evaluateExpressions(template, scenarioVariables);
                generatedProperties.put(p.getName(), value);
            });
            ClientMQRequest clientMQRequest = new ClientMQRequest(message.getQueueName(), messageText, generatedProperties, testId, testIdHeaderName);
            mqClient.request(clientMQRequest);
        }
    }

    static void setMockResponses(WireMockAdmin wireMockAdmin, Project project, String testId, List<MockServiceResponse> responseList, String stepCode, String scenarioName, Map<String, Object> scenarioVariables) throws IOException {
        log.debug("Setting REST-mock responses {} {} {} {}", wireMockAdmin, project, testId, responseList);

        //for priority work it must be >= 1
        //now it's off
        long priority = 0L;
        if (responseList != null && wireMockAdmin != null) {
            for (MockServiceResponse mockServiceResponse : responseList) {
                MockDefinition mockDefinition = new MockDefinition(priority--, project.getTestIdHeaderName(), testId);

                final String url = ExecutorUtils.insertSavedValues(mockServiceResponse.getServiceUrl(), scenarioVariables);
                String complexScenarioName = scenarioName + "_" + stepCode + "_"+ url;
                mockDefinition.setScenarioName(complexScenarioName);

                Integer responseOrder = mockServiceResponse.getResponseOrder();
                if (responseOrder != null && responseOrder > 0){
                    mockDefinition.setRequiredScenarioState(responseOrder > 1 ? "response_order_" + (responseOrder - 1) : null);
                    mockDefinition.setNewScenarioState("response_order_" + responseOrder);
                }

                if (BooleanUtils.isTrue(mockServiceResponse.getUrlPattern())) {
                    mockDefinition.getRequest().setUrlPattern(url);
                } else {
                    mockDefinition.getRequest().setUrl(url);
                }

                mockDefinition.getRequest().setMethod(mockServiceResponse.getHttpMethodOrDefault());

                if(isNotEmpty(mockServiceResponse.getPassword()) || isNotEmpty(mockServiceResponse.getUserName())){
                    BasicAuthCredentials credentials = new BasicAuthCredentials();
                    credentials.setPassword(mockServiceResponse.getPassword());
                    credentials.setUsername(mockServiceResponse.getUserName());
                    mockDefinition.getRequest().setBasicAuthCredentials(credentials);
                }

                RequestMatcher matcher = RequestMatcher.build(mockServiceResponse.getTypeMatching(), mockServiceResponse.getPathFilter());

                if (matcher.isPresent()) {
                    mockDefinition.getRequest().setBodyPatterns(Collections.singletonList(matcher));
                }

                String body = insertSavedValues(mockServiceResponse.getResponseBody(), scenarioVariables);
                mockDefinition.getResponse().setBody(body);
                mockDefinition.getResponse().setStatus(mockServiceResponse.getHttpStatus());

                HashMap<String, String> headers = new HashMap<>();

                if(mockServiceResponse.getHeaders() != null) {
                    mockServiceResponse.getHeaders().forEach(header -> headers.put(header.getHeaderName(), header.getHeaderValue()));
                }
                if(mockServiceResponse.getConvertBase64InMultipart()) {
                    headers.put(MultipartConstant.CONVERT_BASE64_IN_MULTIPART.getValue(), "true");
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

    static void executeSql(Connection connection, Step step, Map<String, Object> scenarioVariables, StepResult stepResult) throws SQLException {
        log.debug("executing sql {}, {}", step, scenarioVariables);
        if (!step.getSqlDataList().isEmpty() && connection != null) {
            List<String> queryList = new LinkedList<>();
            stepResult.setSqlQueryList(queryList);
            for (SqlData sqlData : step.getSqlDataList()) {
                if (StringUtils.isNotEmpty(sqlData.getSql()) && StringUtils.isNotEmpty(sqlData.getSqlSavedParameter())) {
                    String query = evaluateExpressions(sqlData.getSql(), scenarioVariables);
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

    static int calculateNumberRepetitions(Step step, Map<String, Object> scenarioVariables) {
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

    static void setMqMockResponses(WireMockAdmin wireMockAdmin, String testId, List<MqMock> mqMockList, Map<String, Object> scenarioVariables) throws Exception {
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
                            evaluateExpressions(part.getResponseBody(), scenarioVariables),
                            part.getDestinationQueueName()
                    );
                    mockMessage.getResponses().add(definitionPart);
                }
                wireMockAdmin.addMqMapping(mockMessage);
            }
        }
    }
}
