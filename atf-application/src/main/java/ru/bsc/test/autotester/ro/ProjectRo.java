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
import java.util.Map;

/**
 * Created by sdoroshin on 14.09.2017.
 *
 */
@Getter
@Setter
@ApiModel(description = "Representation of project")
public class ProjectRo implements AbstractRo {
    private static final long serialVersionUID = 3953325934454830833L;

    @ApiModelProperty("Unique project code")
    private String code;
    @ApiModelProperty("Project name")
    private String name;
    @ApiModelProperty("Path to scenario which will executing before each scenario in format <group_name>/<scenario_name>")
    private String beforeScenarioPath;
    @ApiModelProperty("Path to scenario which will executing after each scenario in format <group_name>/<scenario_name>")
    private String afterScenarioPath;
    @ApiModelProperty("Properties of test stand")
    private StandRo stand;
    @ApiModelProperty("Shows whether autotester will send random test ID to service")
    private Boolean useRandomTestId;
    @ApiModelProperty("Header name for sending random test ID")
    private String testIdHeaderName;
    @ApiModelProperty("Global headers which service must send with every project's request")
    private String globalRequestHeaders;
    @ApiModelProperty("MQ connection parameters")
    private AmqpBrokerRo amqpBroker;
    @ApiModelProperty("List of scenario groups")
    private List<String> groupList;
    @ApiModelProperty("Variables that will be used for execution of every project`s scenario")
    private Map<String, Object> environmentVariables;
}
