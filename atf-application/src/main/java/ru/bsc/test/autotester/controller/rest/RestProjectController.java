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
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.autotester.configuration.SpringfoxConfig;
import ru.bsc.test.autotester.exception.ResourceNotFoundException;
import ru.bsc.test.autotester.mapper.ProjectRoMapper;
import ru.bsc.test.autotester.ro.ProjectRo;
import ru.bsc.test.autotester.ro.ProjectSearchRo;
import ru.bsc.test.autotester.ro.ScenarioRo;
import ru.bsc.test.autotester.service.ProjectService;
import ru.bsc.test.autotester.service.ScenarioService;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Created by sdoroshin on 12.09.2017.
 *
 */

@Api(tags = SpringfoxConfig.TAG_PROJECT)
@RestController
@RequestMapping("/rest/projects")
public class RestProjectController {
    private final ProjectService projectService;
    private final ScenarioService scenarioService;
    private final ProjectRoMapper projectRoMapper;

    @Autowired
    public RestProjectController(
            ProjectService projectService,
            ScenarioService scenarioService,
            ProjectRoMapper projectRoMapper
    ) {
        this.projectService = projectService;
        this.scenarioService = scenarioService;
        this.projectRoMapper = projectRoMapper;
    }

    @ApiOperation(
            value = "Gets list of stored projects",
            nickname = "findAll",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(code = 200, message = "List of stored projects")
    @RequestMapping
    public List<ProjectRo> findAll() {
        return projectRoMapper.convertProjectListToProjectRoList(projectService.findAll());
    }

    @ApiOperation(
            value = "Gets project by its code",
            nickname = "findOne",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Project data"),
            @ApiResponse(code = 404, message = "Project with given code not found")
    })
    @RequestMapping(value = "{projectCode}", method = RequestMethod.GET)
    public ProjectRo findOne(@PathVariable String projectCode) {
        Project project = projectService.findOne(projectCode);
        if (project != null) {
            return projectRoMapper.projectToProjectRo(project);
        }
        throw new ResourceNotFoundException();
    }

    @ApiOperation(
            value = "Updates existing project with newer data",
            nickname = "saveOne",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponse(code = 200, message = "Updated project")
    @RequestMapping(value = "{projectCode}", method = RequestMethod.PUT)
    public ProjectRo saveOne(@PathVariable String projectCode, @RequestBody ProjectRo projectRo) {
        return projectService.updateFromRo(projectCode, projectRo);
    }

    @ApiOperation(
            value = "Creates new project",
            nickname = "create",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Created project"),
            @ApiResponse(code = 500, message = "Server error. Invalid request")
    })
    @RequestMapping(method = RequestMethod.PUT)
    public ProjectRo create(@RequestBody ProjectRo projectRo) throws IOException {
        return projectService.createFromRo(projectRo);
    }

    @ApiOperation(
            value = "Gets scenarios of given project",
            nickname = "getScenarios",
            produces = MediaType.APPLICATION_JSON_VALUE,
            tags = {SpringfoxConfig.TAG_PROJECT, SpringfoxConfig.TAG_SCENARIO}
    )
    @ApiResponse(code = 200, message = "Scenarios of given project")
    @RequestMapping(value = "{projectCode}/scenarios", method = RequestMethod.GET)
    public List<ScenarioRo> getScenarios(@PathVariable String projectCode) {
        return projectRoMapper.convertScenarioListToScenarioRoList(scenarioService.findAllByProject(projectCode));
    }

    @ApiOperation(
            value = "Creates new scenario tied to given project",
            nickname = "newScenario",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            tags = {SpringfoxConfig.TAG_PROJECT, SpringfoxConfig.TAG_SCENARIO}
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "Saved scenario"),
            @ApiResponse(code = 404, message = "Scenario haven`t saved"),
            @ApiResponse(code = 500, message = "Server error while saving scenario")
    })
    @RequestMapping(value = "{projectCode}/scenarios", method = RequestMethod.POST)
    public ScenarioRo newScenario(@PathVariable String projectCode, @RequestBody ScenarioRo scenarioRo) throws IOException {
        ScenarioRo savedScenario = scenarioService.addScenarioToProject(projectCode, scenarioRo);
        if (savedScenario == null) {
            throw new ResourceNotFoundException();
        }
        return savedScenario;
    }

    @ApiOperation(
            value = "Creates new group tied to given project",
            nickname = "newGroup"
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of groups tied to given project", response = String.class, responseContainer = "list"),
            @ApiResponse(code = 404, message = "Project not exist or group name is empty"),
            @ApiResponse(code = 500, message = "Server error. Error while creating directories for group")
    })
    @RequestMapping(value = "{projectCode}/group", method = RequestMethod.POST)
    public List<String> newGroup(@PathVariable String projectCode, @RequestBody String groupName) throws Exception {
        Project project = projectService.findOne(projectCode);
        if (project != null && StringUtils.isNotEmpty(groupName)) {
            projectService.addNewGroup(projectCode, groupName);

            project = projectService.findOne(projectCode);
            if (project != null) {
                return project.getGroupList();
            } else {
                throw new ResourceNotFoundException();
            }
        }
        throw new ResourceNotFoundException();
    }

    @ApiOperation(
            value = "Rename existing group",
            notes = "Old and new group name is in request body map with keys \"oldGroupName\" and \"newGroupName\" respectively",
            nickname = "renameGroup",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ApiResponses({
            @ApiResponse(code = 200, message = "List of groups tied to given project", response = String.class, responseContainer = "list"),
            @ApiResponse(code = 404, message = "Project not exist or group name is empty"),
            @ApiResponse(code = 500, message = "Server error. Error while renaming group or new group name is not a group")
    })
    @RequestMapping(value = "{projectCode}/group", method = RequestMethod.PUT)
    public List<String> renameGroup(@PathVariable String projectCode, @RequestBody Map<String, String> groupNames) throws Exception {
        Project project = projectService.findOne(projectCode);
        if (project != null && StringUtils.isNotEmpty(groupNames.get("newGroupName"))) {
            projectService.renameGroup(projectCode, groupNames.get("oldGroupName"), groupNames.get("newGroupName"));

            project = projectService.findOne(projectCode);
            if (project != null) {
                return project.getGroupList();
            } else {
                throw new ResourceNotFoundException();
            }
        }
        throw new ResourceNotFoundException();
    }

    @ApiOperation(
            value = "Gets scenarios whose have steps with given relative url",
            nickname = "searchByMethod",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE,
            tags = {SpringfoxConfig.TAG_PROJECT, SpringfoxConfig.TAG_SCENARIO}
    )
    @ApiResponse(code = 200, message = "List of found scenarios")
    @RequestMapping(value = "{projectCode}/search", method = RequestMethod.POST)
    public List<ScenarioRo> searchByMethod(@PathVariable String projectCode, @RequestBody ProjectSearchRo projectSearchRo) {
        return scenarioService.findScenarioByStepRelativeUrl(projectCode, projectSearchRo);
    }

}
