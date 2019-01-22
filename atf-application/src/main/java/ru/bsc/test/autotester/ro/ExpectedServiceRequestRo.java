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

/**
 * Created by sdoroshin on 28.09.2017.
 */
@Getter
@Setter
@ApiModel(description = "Expected request to REST stub")
public class ExpectedServiceRequestRo implements AbstractRo {
    private static final long serialVersionUID = -5748544019274406969L;

    @ApiModelProperty("Unique request code")
    private String code;
    @ApiModelProperty("Name of service being called")
    private String serviceName;
    @ApiModelProperty("Shows that serviceName will be interpreted like RegExp")
    private Boolean urlPattern;
    @ApiModelProperty("Text of request. May be JSON or XML")
    private String expectedServiceRequest;
    @ApiModelProperty("Relative path to file with request text which stored on disk")
    private String expectedServiceRequestFile;
    @ApiModelProperty("Comma separated list of tags which will be ignored while comparing request")
    private String ignoredTags;
    @ApiModelProperty("Count of request repetitions")
    private String count;
    @ApiModelProperty(value = "Type of matching request", allowableValues = "empty, equalToJson, equalToXml, XPath, contains, matches")
    private String typeMatching;
    @ApiModelProperty("Value which will be used to match request. Depends on typeMatching")
    private String pathFilter;
    private Boolean notEvalExprInBody;
    private List<ScenarioVariableFromServiceRequestRo> scenarioVariablesFromServiceRequest;
    private String jsonCompareMode;
}
