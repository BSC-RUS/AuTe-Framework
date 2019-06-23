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
import ru.bsc.test.autotester.diff.Diff;

import java.io.Serializable;
import java.util.List;

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
@ApiModel(description = "Result of step execution")
public class StepResultRo implements Serializable {
    @ApiModelProperty("Random testId")
    private String testId;
    @ApiModelProperty("Step which has been executed")
    private StepRo step;
    @ApiModelProperty(value = "String representation of step result", allowableValues = "OK, Fail")
    private String result;
    @ApiModelProperty("Cause of step fail like log")
    private String details;
    @ApiModelProperty("Expected response")
    private String expected;
    @ApiModelProperty("List of differences between actual and expected responses")
    private List<Diff> diff;
    @ApiModelProperty("Actual response")
    private String actual;
    @ApiModelProperty("Status of actual response")
    private String status;
    @ApiModelProperty("Request URL")
    private String requestUrl;
    @ApiModelProperty("Request body text")
    private String requestBody;
    @ApiModelProperty("Count of polling repetitions")
    private Integer pollingRetryCount;
    @ApiModelProperty("Values of saved parameters")
    private String savedParameters;
    @ApiModelProperty("Description")
    private String description;
    @ApiModelProperty("Shows whether user can change step on result page")
    private boolean editable;
    @ApiModelProperty("Actual cookies")
    private String cookies;
    @ApiModelProperty("List of executed sql queries")
    private List<String> sqlQueryList;

    @ApiModelProperty("List of actual requests made while step executing")
    private List<RequestDataRo> requestDataList;
}
