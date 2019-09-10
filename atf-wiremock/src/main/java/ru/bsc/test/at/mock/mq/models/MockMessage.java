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

package ru.bsc.test.at.mock.mq.models;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

@Data
@ApiModel(description = "Mock MQ message.")
public class MockMessage implements Comparable<MockMessage> {
    @ApiModelProperty("GUID of mock")
    private String guid;
    @ApiModelProperty("JMS mapping name")
    private String name;
    @ApiModelProperty("JMS mapping group")
    private String group;
    @ApiModelProperty("Name of mocked queue")
    private String sourceQueueName;
    @ApiModelProperty("Test identifier where mock is used")
    private String testId;
    @ApiModelProperty("MQ responses")
    private List<MockMessageResponse> responses;
    @ApiModelProperty("HTTP url for request response-body, if responseBody is empty")
    private String httpUrl;
    @ApiModelProperty("XPath expression to evaluate with message")
    private String xpath;
    @ApiModelProperty("Mock priority")
    private Integer priority;

    @Override
    public int compareTo(MockMessage message) {
        return Integer.compare(getPriorityOrDefault(), message.getPriorityOrDefault());
    }

    public boolean hasGroup() {
        return StringUtils.isNotEmpty(group);
    }

    private Integer getPriorityOrDefault() {
        return priority != null ? priority : Integer.MAX_VALUE;
    }
}
