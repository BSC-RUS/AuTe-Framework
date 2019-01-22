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

package ru.bsc.test.autotester.controller.rest;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import ru.bsc.test.at.model.Version;
import ru.bsc.test.autotester.configuration.SpringfoxConfig;
import ru.bsc.test.autotester.service.VersionService;
import ru.bsc.test.autotester.service.impl.WiremockVersion;

import java.util.List;

/**
 * Created by sdoroshin on 23.10.2017.
 *
 */

@Api(tags = SpringfoxConfig.TAG_VERSION)
@RestController
@RequestMapping("/rest/version")
public class RestVersionController {

    private final VersionService versionService;

    @Autowired
    public RestVersionController(VersionService versionService) {
        this.versionService = versionService;
    }

    @ApiOperation(
            value = "Gets application version",
            nickname = "managerVersion",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Version of the application"),
    })
    @RequestMapping(value = "/application", method = RequestMethod.GET)
    public Version managerVersion() {
        return versionService.getApplicationVersion();
    }

    @ApiOperation(
            value = "Gets list of wiremock versions",
            nickname = "getWiremockVersions",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of wiremock versions"),
    })
    @RequestMapping(value = "/wiremock", method = RequestMethod.GET)
    public List<WiremockVersion> getWiremockVersions() {
        return versionService.getWiremockVersions();
    }
}
