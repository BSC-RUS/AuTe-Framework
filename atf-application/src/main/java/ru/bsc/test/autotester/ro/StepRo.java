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

package ru.bsc.test.autotester.ro;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
@ApiModel(description = "Scenario step")
public class StepRo implements AbstractRo {
    private static final long serialVersionUID = -4795596079038167133L;

    @ApiModelProperty("Unique code")
    private String code;
    @ApiModelProperty("Relative URL of service being tested (Available if stepMode = REST)). It`s possible to use scenario variables like /rest/items/{itemId}")
    private String relativeUrl;
    @ApiModelProperty(value = "Method of request", allowableValues = "POST, GET, PUT, DELETE, PATCH")
    private String requestMethod;
    @ApiModelProperty("Text of request body. May be JSON and XML")
    private String request;
    @ApiModelProperty("Relative path to file with request text which stored on disk")
    private String requestFile;
    @ApiModelProperty("Headers which service must send with request")
    private String requestHeaders;
    @ApiModelProperty("Text of response body which expects from service")
    private String expectedResponse;
    @ApiModelProperty("Relative path to file with response text which stored on disk")
    private String expectedResponseFile;
    @ApiModelProperty("Response comparison won`t be done")
    private Boolean expectedResponseIgnore;
    @ApiModelProperty("Expected HTTP status of the request. (Available if stepMode = REST)")
    private Integer expectedStatusCode;
    @ApiModelProperty("Variables which receive from response. Path to variable value sets in XPath. Example: parameterName = $.element.items[2].title")
    private String jsonXPath;
    @ApiModelProperty(value = "Type of request (Available if stepMode = REST)", allowableValues = "JSON, FORM-data")
    private String requestBodyType;
    @ApiModelProperty("If enabled, request will be repeat while XPath JSON parameter not found")
    private Boolean usePolling;
    @ApiModelProperty("Count of polling repetitions")
    private String pollingRetryCount;
    @ApiModelProperty("XPath according to which polling works")
    private String pollingJsonXPath;
    @ApiModelProperty("List of dynamic REST stubs")
    private List<MockServiceResponseRo> mockServiceResponseList = new ArrayList<>();
    @ApiModelProperty("Shows whether step will be executed")
    private Boolean disabled;
    @ApiModelProperty("Description")
    private String stepComment;
    @ApiModelProperty("Scenario variables and their expected values which need compare to actual")
    private Map<String, String> savedValuesCheck;
    @ApiModelProperty("List of different data sets for multiple step execution")
    private List<StepParameterSetRo> stepParameterSetList = new ArrayList<>();
    @ApiModelProperty("Check request order or not")
    private Boolean checkRequestsOrder;
    @ApiModelProperty("Ignore expected requests")
    private Boolean ignoreRequestsInvocations;
    @ApiModelProperty("List of expected requests to REST stubs")
    private List<ExpectedServiceRequestRo> expectedServiceRequestList = new ArrayList<>();
    @ApiModelProperty("List of MQ messages, which will be sent to queue")
    private List<MqMessageRo> mqMessages = new ArrayList<>();
    @ApiModelProperty(value = "Mode of response comparison", allowableValues = "JSON, Full match, Mask *ignore*")
    private String responseCompareMode;
    @ApiModelProperty("List of form-data items. Available if requestBodyType = FORM-data")
    private List<FormDataRo> formDataList;
    @ApiModelProperty("List of SQL calls to database")
    private List<SQLDataRo> sqlDataList;
    @ApiModelProperty("Shows whether type of REST request form will be multipart/form-data")
    private Boolean multipartFormData;
    @ApiModelProperty(value = "Mode of response comparison if responseCompareMode = JSON", allowableValues = "NON_EXTENSIBLE, STRICT, LENIENT, STRICT_ORDER")
    private String jsonCompareMode;
    @ApiModelProperty("Javascript for additional flexible checks")
    private String script;
    @ApiModelProperty("Number of step repetitions. It`s possible to use scenario variables")
    private String numberRepetitions;
    private String timeoutMs;
    @ApiModelProperty("Timeout before each step repetition in millis")
    private boolean timeoutEachRepetition;
    @ApiModelProperty("List of responses which MQ stub returns")
    private List<MqMockRo> mqMockResponseList = new ArrayList<>();
    @ApiModelProperty("List of requests to MQ stub that need to check")
    private List<ExpectedMqRequestRo> expectedMqRequestList;
    @ApiModelProperty("If enabled, MQ requests which no need to check will be ignored")
    private Boolean ignoreUndeclaredMqRequests;
    @ApiModelProperty("List of scenario variables definitions which need to save from request to MQ stub")
    private List<ScenarioVariableFromMqRequestRo> scenarioVariableFromMqRequestList;
    @ApiModelProperty(value = "Type", allowableValues = "REST, REST ASYNC, JMS")
    private String stepMode;
    @ApiModelProperty("For REST ASYNC step type. Wiremock polling timeout")
    private String mockPollingTimeout;
    @ApiModelProperty("For REST ASYNC step type. Delay after wiremock polling")
    private String mockRetryDelay;

    @ApiModelProperty("Deprecated")
    @Deprecated
    private String sql;
    @ApiModelProperty("Deprecated")
    @Deprecated
    private String sqlSavedParameter;
    @ApiModelProperty("Deprecated")
    @Deprecated
    private String mqName;
    @ApiModelProperty("Deprecated")
    @Deprecated
    private String mqMessage;
    @ApiModelProperty("Deprecated")
    @Deprecated
    private String mqMessageFile;
    @ApiModelProperty("Deprecated")
    @Deprecated
    private List<NameValuePropertyRo> mqPropertyList;
    @ApiModelProperty("Use Response as base64")
    private boolean useResponseAsBase64;

    @ApiModelProperty("The queue in which the message is sent")
    private String mqOutputQueueName;
    @ApiModelProperty("The queue from which the message is expected")
    private String mqInputQueueName;
    @ApiModelProperty("Timeout before queue check (Available if stepMode = JMS). It`s possible to use scenario variables")
    private String mqTimeoutMs;
}
