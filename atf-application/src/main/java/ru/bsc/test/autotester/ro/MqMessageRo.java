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
import ru.bsc.test.at.executor.model.NameValueProperty;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by smakarov
 * 20.04.2018 12:22
 */
@Getter
@Setter
@ApiModel(description = "MQ message, which will be sent to queue")
public class MqMessageRo implements AbstractRo {
    private static final long serialVersionUID = -1549749532101759753L;

    @ApiModelProperty("Name of queue in which message will be sent")
    private String queueName;
    @ApiModelProperty("Message text")
    private String message;
    @ApiModelProperty("Relative path to file with message text which stored on disk")
    private String messageFile;
    @ApiModelProperty("List of properties which will be sent to queue with message")
    private List<NameValueProperty> properties = new LinkedList<>();
}
