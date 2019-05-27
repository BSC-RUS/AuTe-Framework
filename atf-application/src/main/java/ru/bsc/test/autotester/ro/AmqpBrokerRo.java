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

@Getter
@Setter
@ApiModel(description = "MQ connection parameters")
public class AmqpBrokerRo implements AbstractRo {
    private static final long serialVersionUID = -6295730897412089910L;

    @ApiModelProperty(value = "MQ broker type", allowableValues = "IBM_MQ, RABBIT_MQ, ACTIVE_MQ")
    private String mqService;
    @ApiModelProperty("MQ server address")
    private String host;
    @ApiModelProperty("MQ server port")
    private Integer port;
    @ApiModelProperty("MQ server username")
    private String username;
    @ApiModelProperty("MQ server password")
    private String password;
    @ApiModelProperty("MQ server channel")
    private String channel;
    @ApiModelProperty("The timeout value (in milliseconds), a time out of zero never expires.")
    private long maxTimeoutWait;
    @ApiModelProperty("Use camel naming policy for MQ properties.")
    private boolean useCamelNamingPolicyIbmMQ;
}
