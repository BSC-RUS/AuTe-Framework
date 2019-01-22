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
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
@ApiModel(description = "Test scenario")
public class ScenarioRo implements AbstractRo {
    private static final long serialVersionUID = -6026744701723398082L;

    @ApiModelProperty("Unique scenario code")
    private String code;
    @ApiModelProperty("Scenario project code")
    private String projectCode;
    @ApiModelProperty("Scenario name")
    private String name;

    @ApiModelProperty("Scenario group name")
    private String scenarioGroup;
    @ApiModelProperty("List of scenario steps")
    private List<StepRo> stepList;
    @ApiModelProperty("Shows whether scenario before will be ignored")
    private Boolean beforeScenarioIgnore;
    @ApiModelProperty("Shows whether scenario after will be ignored")
    private Boolean afterScenarioIgnore;
    @ApiModelProperty("Shows whether scenario was failed while execution")
    private Boolean failed;
    @ApiModelProperty("Shows whether scenario has execution results")
    private Boolean hasResults;
}
