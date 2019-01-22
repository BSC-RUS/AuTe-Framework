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

import com.google.gson.Gson;
import io.swagger.annotations.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bsc.test.autotester.configuration.SpringfoxConfig;
import ru.bsc.test.autotester.mapper.ExecutionResultRoMapper;
import ru.bsc.test.autotester.mapper.StepRoMapper;
import ru.bsc.test.autotester.ro.ExecutionResultRo;
import ru.bsc.test.autotester.ro.ScenarioIdentityRo;
import ru.bsc.test.autotester.ro.StepResultRo;
import ru.bsc.test.autotester.service.ScenarioService;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.zip.ZipOutputStream;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * Created by sdoroshin on 23.01.2018.
 *
 */

@Api(tags = SpringfoxConfig.TAG_EXECUTION)
@RestController
@RequestMapping("/rest/execution/")
public class RestExecutionController {

    private final Gson gson = new Gson();
    private final ScenarioService scenarioService;
    private final ExecutionResultRoMapper executionResultRoMapper;
    private final StepRoMapper stepRoMapper;

    @Autowired
    public RestExecutionController(
            ScenarioService scenarioService,
            ExecutionResultRoMapper executionResultRoMapper,
            StepRoMapper stepRoMapper
    ) {
        this.scenarioService = scenarioService;
        this.executionResultRoMapper = executionResultRoMapper;
        this.stepRoMapper = stepRoMapper;
    }

    @ApiOperation(
            value = "Stops executing tests by execution UUID",
            nickname = "stopExecuting"
    )
    @ApiResponse(code = 200, message = "Execution stopped")
    @RequestMapping(value = "{executionUuid}/stop", method = POST)
    public void stopExecuting(@PathVariable String executionUuid) {
        scenarioService.stopExecuting(executionUuid);
    }

    @ApiOperation(
            value = "Gets status of execution by its ID",
            notes = "Status and results of running execution",
            nickname = "getStatus",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(code = 200, message = "Status of execution")
    @RequestMapping(value = "{executionUuid}/status", method = GET)
    public ExecutionResultRo getStatus(@PathVariable String executionUuid) {
        return executionResultRoMapper.map(scenarioService.getResult(executionUuid));
    }

    @ApiOperation(
            value = "Gets result of execution by its ID",
            notes = "Results of stored execution",
            nickname = "getResults",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Result of execution"),
            @ApiResponse(code = 500, message = "Server error. Cannot read result file")
    })
    @RequestMapping(value = "/results", method = POST)
    public List<StepResultRo> getResults(@RequestBody ScenarioIdentityRo identity) {
        return stepRoMapper.convertStepResultListToStepResultRoWithDiff(scenarioService.getResult(identity));
    }

    @ApiOperation(
            value = "Generates Allure report",
            notes = "Report generate for scenarios, identify by parameter, and store",
            nickname = "getReport",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Report has been generated", responseHeaders = @ResponseHeader(name = HttpHeaders.CONTENT_DISPOSITION)),
            @ApiResponse(code = 404, message = "Cannot find scenarios to generate report"),
            @ApiResponse(code = 500, message = "Server error. Cannot read scenario file or generate report")
    })
    @RequestMapping(value = "/report", method = POST, produces="application/zip")
    public void getReport(@RequestBody List<ScenarioIdentityRo> identities, HttpServletResponse response) throws Exception {
        response.addHeader("Content-Disposition", "attachment; filename=\"report.zip\"");
        try (ZipOutputStream zipOutputStream = new ZipOutputStream(response.getOutputStream())) {
            scenarioService.getReport(identities, zipOutputStream);
        }
    }

    @ApiOperation(
            value = "Gets list of executing scenarios",
            nickname = "list",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(code = 200, message = "List of executing scenarios", response = String.class, responseContainer = "list")
    @RequestMapping(value = "list", method = GET)
    public String list() {
        return gson.toJson(scenarioService.getExecutingList());
    }
}
