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

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.autotester.exception.ResourceNotFoundException;
import ru.bsc.test.autotester.mapper.ProjectRoMapper;
import ru.bsc.test.autotester.mapper.StepRoMapper;
import ru.bsc.test.autotester.ro.ScenarioRo;
import ru.bsc.test.autotester.ro.StartScenarioInfoRo;
import ru.bsc.test.autotester.ro.StepRo;
import ru.bsc.test.autotester.service.ProjectService;
import ru.bsc.test.autotester.service.ScenarioService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
 * Created by sdoroshin on 14.09.2017.
 *
 */

@RestController
@RequestMapping("/rest/projects/{projectCode}/scenarios")
public class RestScenarioController {

    private final ScenarioService scenarioService;
    private final ProjectService projectService;
    private final StepRoMapper stepRoMapper;
    private final ProjectRoMapper projectRoMapper;

    @Autowired
    public RestScenarioController(
            ScenarioService scenarioService,
            ProjectService projectService,
            StepRoMapper stepRoMapper,
            ProjectRoMapper projectRoMapper
    ) {
        this.scenarioService = scenarioService;
        this.projectService = projectService;
        this.stepRoMapper = stepRoMapper;
        this.projectRoMapper = projectRoMapper;
    }

    @RequestMapping(value = { "{scenarioCode:.+}/steps", "{scenarioGroup:.+}/{scenarioCode:.+}/steps" }, method = RequestMethod.GET)
    public List<StepRo> findSteps(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        Scenario scenario = scenarioService.findOne(projectCode, scenarioPath);
        if (scenario != null) {
            return stepRoMapper.convertStepListToStepRoList(scenario.getStepList());
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(value = { "{scenarioCode:.+}", "{scenarioGroup:.+}/{scenarioCode:.+}" }, method = RequestMethod.GET)
    public ScenarioRo findOne(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        Scenario scenario = scenarioService.findOne(projectCode, scenarioPath);
        if (scenario != null) {
            // TODO Set projectName
            return projectRoMapper.scenarioToScenarioRo(projectCode, scenario);
        }
        throw new ResourceNotFoundException();
    }

    @RequestMapping(value = { "{scenarioCode:.+}", "{scenarioGroup:.+}/{scenarioCode:.+}" }, method = RequestMethod.PUT)
    public ScenarioRo saveOne(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode,
            @RequestBody ScenarioRo scenarioRo) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode + "/";
        ScenarioRo savedScenario = scenarioService.updateScenarioFormRo(projectCode, scenarioPath, scenarioRo);
        if (savedScenario == null) {
            throw new ResourceNotFoundException();
        }
        return savedScenario;
    }

    @RequestMapping(value = { "{scenarioCode:.+}", "{scenarioGroup:.+}/{scenarioCode:.+}" }, method = RequestMethod.DELETE)
    public void deleteOne(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        scenarioService.deleteOne(projectCode, scenarioPath);
    }

    @RequestMapping(value = { "{scenarioCode:.+}/steps", "{scenarioGroup:.+}/{scenarioCode:.+}/steps" }, method = RequestMethod.POST)
    //TODO не используется. Удалить
    public StepRo createNewStep(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode,
            @RequestBody StepRo stepRo) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        StepRo savedStep = scenarioService.addStepToScenario(projectCode, scenarioPath, stepRo);
        if (savedStep == null) {
            throw new ResourceNotFoundException();
        }
        return savedStep;
    }

    @RequestMapping(value = { "{scenarioCode:.+}/steps", "{scenarioGroup:.+}/{scenarioCode:.+}/steps" }, method = RequestMethod.PUT)
    public List<StepRo> saveStepList(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode,
            @RequestBody List<StepRo> stepRoList) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        return scenarioService.updateStepListFromRo(projectCode, scenarioPath, stepRoList);
    }

    @RequestMapping(value = { "{scenarioCode:.+}/start", "{scenarioGroup:.+}/{scenarioCode:.+}/start" }, method = RequestMethod.POST)
    public StartScenarioInfoRo executing(
            @PathVariable String projectCode,
            @PathVariable(required = false) String scenarioGroup,
            @PathVariable String scenarioCode) throws IOException {
        String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + scenarioCode;
        Scenario scenario = scenarioService.findOne(projectCode, scenarioPath);
        // TODO Сохранить сценарий для обновления всех путей
        scenario = scenarioService.saveScenario(projectCode, scenarioPath, scenario);
        Project project = projectService.findOne(projectCode);
        if (scenario != null) {
            return scenarioService.startScenarioExecutingList(project, Collections.singletonList(scenario));
        }
        throw new ResourceNotFoundException();
    }
}
