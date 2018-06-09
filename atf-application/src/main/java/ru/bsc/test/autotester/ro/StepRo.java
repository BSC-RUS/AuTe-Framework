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

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
public class StepRo implements AbstractRo {
    private static final long serialVersionUID = -4795596079038167133L;

    private String code;
    private String relativeUrl;
    private String requestMethod;
    private String request;
    private String requestFile;
    private String requestHeaders;
    private String expectedResponse;
    private String expectedResponseFile;
    private Boolean expectedResponseIgnore;
    private Integer expectedStatusCode;
    private String jsonXPath;
    private String requestBodyType;
    private Boolean usePolling;
    private String pollingJsonXPath;
    private List<MockServiceResponseRo> mockServiceResponseList = new ArrayList<>();
    private Boolean disabled;
    private String stepComment;
    private Map<String, String> savedValuesCheck;
    private List<StepParameterSetRo> stepParameterSetList = new ArrayList<>();
    private List<ExpectedServiceRequestRo> expectedServiceRequestList = new ArrayList<>();
    private List<MqMessageRo> mqMessages = new ArrayList<>();
    private String responseCompareMode;
    private List<FormDataRo> formDataList;
    private List<SQLDataRo> sqlDataList;
    private Boolean multipartFormData;
    private String jsonCompareMode;
    private String script;
    private String numberRepetitions;
    private String parseMockRequestUrl;
    private String parseMockRequestXPath;
    private String parseMockRequestScenarioVariable;
    private String timeoutMs;
    private List<MqMockRo> mqMockResponseList = new ArrayList<>();
    private List<ExpectedMqRequestRo> expectedMqRequestList;
    private List<ScenarioVariableFromMqRequestRo> scenarioVariableFromMqRequestList;
    private String stepMode;

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
    private List<NameValuePropertyRo> mqPropertyList;

    private String mqOutputQueueName;
    private String mqInputQueueName;
    private String mqTimeoutMs;
}
