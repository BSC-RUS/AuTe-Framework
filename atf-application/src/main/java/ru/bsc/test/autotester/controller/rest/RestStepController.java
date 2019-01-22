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
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.bsc.test.autotester.configuration.SpringfoxConfig;
import ru.bsc.test.autotester.exception.ResourceNotFoundException;
import ru.bsc.test.autotester.mapper.StepRoMapper;
import ru.bsc.test.autotester.ro.StepRo;
import ru.bsc.test.autotester.service.ScenarioService;

import java.io.IOException;

@Api(tags = SpringfoxConfig.TAG_STEP)
@RestController
@RequestMapping("/rest/projects/{projectCode}/scenarios")
public class RestStepController {

    private final ScenarioService scenarioService;
    private final StepRoMapper stepRoMapper;

    @Autowired
    public RestStepController(ScenarioService scenarioService, StepRoMapper stepRoMapper) {
        this.scenarioService = scenarioService;
        this.stepRoMapper = stepRoMapper;
    }

    @ApiOperation(
            value = "Updates step for given code",
            nickname = "updateOne",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated step"),
            @ApiResponse(code = 404, message = "Step wasn`t found in given scenario"),
            @ApiResponse(code = 500, message = "Server error. Cannot read step`s scenario from file, empty scenario name or troubles while saving")
    })
    @RequestMapping(value = { "{scenarioGroup:.+}/{scenarioCode:.+}/steps/{stepCode:.+}", "{scenarioCode:.+}/steps/{stepCode:.+}" }, method = RequestMethod.PUT)
    public StepRo updateOne(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode,
            @PathVariable String stepCode,
            @RequestBody StepRo stepRo) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        StepRo updatedStep = scenarioService.updateStepFromRo(projectCode, scenarioPath, stepCode, stepRo);
        if (updatedStep == null) {
            throw new ResourceNotFoundException();
        }
        return updatedStep;
    }

    @ApiOperation(
            value = "Clones step with given code",
            nickname = "cloneStep",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Updated step"),
            @ApiResponse(code = 500, message = "Server error. Cannot read step`s scenario from file")
    })
    @RequestMapping(value = { "{scenarioGroup:.+}/{scenarioCode:.+}/steps/{stepCode:.+}/clone", "{scenarioCode:.+}/steps/{stepCode:.+}/clone" }, method = RequestMethod.POST)
    public StepRo cloneStep(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode,
            @PathVariable String stepCode) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        return stepRoMapper.stepToStepRo(scenarioService.cloneStep(projectCode, scenarioPath, stepCode));
    }
}
