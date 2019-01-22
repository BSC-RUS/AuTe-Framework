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
@ApiModel(description = "Request to MQ stub that need to check")
public class ExpectedMqRequestRo {
    @ApiModelProperty("Unique MQ request code")
    private String code;
    @ApiModelProperty("Name of service being tested queue, which define in sourceQueueName param of the properties.yml")
    private String sourceQueue;
    @ApiModelProperty("Text of request body")
    private String requestBody;
    @ApiModelProperty("Comma separated list of tags which will be ignored while comparing request")
    private String ignoredTags;
    @ApiModelProperty("Count of request repetitions")
    private String count;
}
