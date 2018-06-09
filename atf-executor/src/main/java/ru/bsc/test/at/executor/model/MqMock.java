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

import lombok.Getter;
import lombok.Setter;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by sdoroshin on 27.07.2017.
 */
@Getter
@Setter
public class MqMock implements AbstractModel, CodeAccessible {

    private String code;
    private String sourceQueueName;
    private List<MqMockResponse> responses = new LinkedList<>();
    private String httpUrl;
    private String xpath;

    @Deprecated
    private String responseBody;
    @Deprecated
    private String destinationQueueName;

    protected MqMock copy() {
        MqMock response = new MqMock();
        response.setSourceQueueName(getSourceQueueName());
        response.setHttpUrl(getHttpUrl());
        getResponses().forEach(r -> response.getResponses().add(r.copy()));
        response.setXpath(getXpath());

        response.setResponseBody(getResponseBody());
        response.setDestinationQueueName(getDestinationQueueName());
        return response;
    }
}
