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

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@ApiModel(description = "Responses which MQ stub returns")
public class MqMockRo {
    @ApiModelProperty("Unique response code")
    private String code;
    @ApiModelProperty("Name of service being tested queue, which define in sourceQueueName param of the properties.yml")
    private String sourceQueueName;
    @ApiModelProperty("Full path to service which must send message to queue")
    private String httpUrl;
    @ApiModelProperty("Mask to filtering messages in queue")
    private String xpath;
    @ApiModelProperty("List of responses")
    private List<MqMockResponseRo> responses;

    @ApiModelProperty("Deprecated")
    @Deprecated
    private String responseBody;
    @ApiModelProperty("Deprecated")
    @Deprecated
    private String destinationQueueName;
}
