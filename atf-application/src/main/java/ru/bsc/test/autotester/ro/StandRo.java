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

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
@ApiModel(description = "Properties of test stand")
public class StandRo implements AbstractRo {
    private static final long serialVersionUID = 4390819697062478918L;

    @ApiModelProperty("URL of service being tested")
    private String serviceUrl;
    @ApiModelProperty("Connection URL of database")
    private String dbUrl;
    @ApiModelProperty("Username for connection to database")
    private String dbUser;
    @ApiModelProperty("Password for connection to database")
    private String dbPassword;
    @ApiModelProperty("Url to BSC WireMock")
    private String wireMockUrl;
}
