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

package ru.bsc.test.at.executor.model;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import lombok.Data;
import ru.bsc.test.at.executor.helper.client.impl.http.HTTPMethod;

/**
 * Created by sdoroshin on 10.05.2017.
 */
@Data
public class Step implements Serializable, AbstractModel {
    private static final long serialVersionUID = 1670286319126044952L;

    private String code;
    private Boolean checkRequestsOrder;
    private Boolean ignoreRequestsInvocations;
    private Boolean ignoreMqRequestsInvocations; // используется для обратной совместимости
    private List<ExpectedServiceRequest> expectedServiceRequests = new LinkedList<>();
    private String relativeUrl;
    private HTTPMethod requestMethod;
    private String request;
    private String requestFile;
    private String requestHeaders;
    private String expectedResponse;
    private String expectedResponseFile;
    private Boolean expectedResponseIgnore;
    private Integer expectedStatusCode;
    private String jsonXPath;
    private RequestBodyType requestBodyType = RequestBodyType.JSON;
    private Boolean usePolling;
    private String pollingRetryCount;
    private String pollingJsonXPath;
    private List<MockServiceResponse> mockServiceResponseList = new LinkedList<>();
    private Boolean disabled;
    private String stepComment;
    private Map<String, String> savedValuesCheck = new LinkedHashMap<>();
    private ResponseCompareMode responseCompareMode = ResponseCompareMode.JSON;
    private List<StepParameterSet> stepParameterSetList = new LinkedList<>();
    private List<MqMessage> mqMessages = new LinkedList<>();
    private Boolean multipartFormData;
    private List<FormData> formDataList = new LinkedList<>();
    private String jsonCompareMode = "NON_EXTENSIBLE";
    private String script;
    private String numberRepetitions;
    private String timeoutMs;
    private boolean timeoutEachRepetition;
    private List<MqMock> mqMockResponseList = new LinkedList<>();
    private List<ExpectedMqRequest> expectedMqRequestList;
    private Boolean ignoreUndeclaredMqRequests = false;
    private List<SqlData> sqlDataList = new LinkedList<>();
    private List<ScenarioVariableFromMqRequest> scenarioVariableFromMqRequestList;
    private StepMode stepMode;
    private String mockPollingTimeout;
    private String mockRetryDelay;
    private boolean useResponseAsBase64;

    @Deprecated
    private String sql;
    @Deprecated
    private String sqlSavedParameter;
    @Deprecated
    private String mqName;
    @Deprecated
    private String mqMessage;
    @Deprecated
    private String mqMessageFile;
    @Deprecated
    private List<NameValueProperty> mqPropertyList = new LinkedList<>();

    // JMS step mode
    private String mqOutputQueueName;
    private String mqInputQueueName;
    private String mqTimeoutMs;

    public Step copy() {
        Step step = new Step();
        step.setRelativeUrl(getRelativeUrl());
        step.setRequestMethod(getRequestMethod());
        step.setRequestHeaders(getRequestHeaders());
        step.setRequest(getRequest());
        step.setExpectedResponse(getExpectedResponse());
        step.setExpectedStatusCode(getExpectedStatusCode());
        step.setJsonXPath(getJsonXPath());
        step.setRequestBodyType(getRequestBodyType());
        step.setExpectedResponseIgnore(getExpectedResponseIgnore());
        step.setUsePolling(getUsePolling());
        step.setPollingJsonXPath(getPollingJsonXPath());
        step.setDisabled(getDisabled());
        step.setStepComment(getStepComment());
        step.setScript(getScript());
        step.setSavedValuesCheck(new HashMap<>(getSavedValuesCheck()));
        step.setResponseCompareMode(getResponseCompareMode());
        step.setTimeoutEachRepetition(isTimeoutEachRepetition());

        List<ExpectedServiceRequest> expectedServiceRequests = Optional.ofNullable(getExpectedServiceRequests()).orElse(new LinkedList<>());
        step.setExpectedServiceRequests(expectedServiceRequests.stream().map(ExpectedServiceRequest::copy).collect(Collectors.toCollection(LinkedList::new)));


        if (getMockServiceResponseList() != null) {
            step.setMockServiceResponseList(new LinkedList<>());
            for (MockServiceResponse mockServiceResponse : getMockServiceResponseList()) {
                step.getMockServiceResponseList().add(mockServiceResponse.copy());
            }
        }
        if (getStepParameterSetList() != null) {
            step.setStepParameterSetList(new LinkedList<>());
            for (StepParameterSet stepParameterSet : getStepParameterSetList()) {
                step.getStepParameterSetList().add(stepParameterSet.copy());
            }
        }
        if (getFormDataList() != null) {
            step.setFormDataList(new LinkedList<>());
            for (FormData formData : getFormDataList()) {
                step.getFormDataList().add(formData.copy());
            }
        }
        if (getMqMessages() != null) {
            step.setMqMessages(new LinkedList<>());
            getMqMessages().forEach(info -> step.getMqMessages().add(info.copy()));
        }
        if (getMqPropertyList() != null) {
            step.setMqPropertyList(new LinkedList<>());
            for (NameValueProperty property : getMqPropertyList()) {
                step.getMqPropertyList().add(property.copy());
            }
        }
        if (getSqlDataList() != null) {
            step.setSqlDataList(new LinkedList<>());
            for (SqlData sqlData : getSqlDataList()) {
                step.getSqlDataList().add(sqlData.copy());
            }
        }

        step.setMqName(getMqName());
        step.setMqMessage(getMqMessage());
        step.setMqMessageFile(getMqMessageFile());
        step.setMultipartFormData(getMultipartFormData());
        step.setJsonCompareMode(getJsonCompareMode());
        step.setNumberRepetitions(getNumberRepetitions());
        step.setTimeoutMs(getTimeoutMs());
        step.setStepMode(getStepMode());

        if (this.getMqMockResponseList() == null) {
            step.setMqMockResponseList(new LinkedList<>());
        }
        this.getMqMockResponseList().clear();
        if (this.getMqMockResponseList() != null) {
            for (MqMock mqMock : this.getMqMockResponseList()) {
                this.getMqMockResponseList().add(mqMock.copy());
            }
        }

        if (getExpectedMqRequestList() != null) {
            step.setExpectedMqRequestList(new LinkedList<>());
            for (ExpectedMqRequest expectedMqRequest : getExpectedMqRequestList()) {
                step.getExpectedMqRequestList().add(expectedMqRequest.copy());
            }
        }

        step.setIgnoreUndeclaredMqRequests(getIgnoreUndeclaredMqRequests());

        if (getSqlDataList() != null) {
            step.setSqlDataList(new LinkedList<>());
            for (SqlData sqlData : getSqlDataList()) {
                step.getSqlDataList().add(sqlData.copy());
            }
        }

        step.setSql(getSql());
        step.setSqlSavedParameter(getSqlSavedParameter());

        if (getScenarioVariableFromMqRequestList() != null) {
            step.setScenarioVariableFromMqRequestList(new LinkedList<>());
            for (ScenarioVariableFromMqRequest variable : getScenarioVariableFromMqRequestList()) {
                step.getScenarioVariableFromMqRequestList().add(variable.copy());
            }
        }

        step.setMockPollingTimeout(getMockPollingTimeout());
        step.setMockRetryDelay(getMockRetryDelay());
        step.setCheckRequestsOrder(getCheckRequestsOrder());
        step.setIgnoreRequestsInvocations(getIgnoreRequestsInvocations());
        step.setIgnoreMqRequestsInvocations(getIgnoreMqRequestsInvocations());
        return step;
    }

    public Boolean getExpectedResponseIgnore() {
        return expectedResponseIgnore != null && expectedResponseIgnore;
    }

    public Boolean getUsePolling() {
        return usePolling != null && usePolling;
    }

    public Boolean getDisabled() {
        return disabled != null && disabled;
    }

    public Boolean getMultipartFormData() {
        return multipartFormData != null && multipartFormData;
    }

    public StepMode getStepMode() {
        return stepMode == null ? StepMode.REST : stepMode;
    }

    public Boolean getIgnoreUndeclaredMqRequests(){
        return Boolean.TRUE.equals(ignoreUndeclaredMqRequests) || Boolean.TRUE.equals(ignoreMqRequestsInvocations);
    }
}
