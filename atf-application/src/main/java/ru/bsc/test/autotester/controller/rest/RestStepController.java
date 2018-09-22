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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.bsc.test.autotester.exception.ResourceNotFoundException;
import ru.bsc.test.autotester.mapper.StepRoMapper;
import ru.bsc.test.autotester.ro.StepRo;
import ru.bsc.test.autotester.service.ScenarioService;

import java.io.IOException;

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
