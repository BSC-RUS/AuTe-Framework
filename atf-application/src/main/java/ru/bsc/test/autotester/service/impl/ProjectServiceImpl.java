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

package ru.bsc.test.autotester.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.autotester.mapper.ProjectRoMapper;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.repository.ProjectRepository;
import ru.bsc.test.autotester.ro.ProjectRo;
import ru.bsc.test.autotester.service.ProjectService;

import java.io.IOException;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.util.List;

/**
 * Created by sdoroshin on 21.03.2017.
 *
 */
@Service
@Slf4j
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectRoMapper projectRoMapper;
    private final EnvironmentProperties environmentProperties;

    @Autowired
    public ProjectServiceImpl(ProjectRepository projectRepository, ProjectRoMapper projectRoMapper, EnvironmentProperties environmentProperties) {
        this.projectRepository = projectRepository;
        this.projectRoMapper = projectRoMapper;
        this.environmentProperties = environmentProperties;
    }

    @Override
    public List<Project> findAll() {
        synchronized (this) {
            return projectRepository.findAllProjects();
        }
    }

    @Override
    public Project findOne(String projectCode) {
        synchronized (this) {
            return projectRepository.findProject(projectCode);
        }
    }

    @Override
    public ProjectRo updateFromRo(String projectCode, ProjectRo projectRo) {
        synchronized (this) {
            Project project = findOne(projectCode);
            if (project != null) {
                project = projectRoMapper.updateProjectFromRo(projectRo);
                projectRepository.saveProject(project);
                project = projectRepository.findProject(project.getCode());
                return projectRoMapper.projectToProjectRo(project);
            }
            return null;
        }
    }

    @Override
    public ProjectRo createFromRo(ProjectRo projectRo) throws IOException {
        synchronized (this) {
            Project project = projectRoMapper.updateProjectFromRo(projectRo);

            if (StringUtils.isBlank(project.getCode())) {
                throw new IOException("Empty project code");
            }

            if (StringUtils.isBlank(project.getName())) {
                throw new IOException("Empty project name");
            }

            try {
                if (Paths.get(environmentProperties.getProjectsDirectoryPath(), project.getCode()).toFile().exists()) {
                    throw new IOException("Directory already exists");
                }
            } catch (InvalidPathException e) {
                log.warn("Create project exception: {}", e.getMessage());
                throw new IOException("Wrong project code");
            }

            projectRepository.saveProject(project);
            project = projectRepository.findProject(project.getCode());
            return projectRoMapper.projectToProjectRo(project);
        }
    }

    @Override
    public void addNewGroup(String projectCode, String groupName) throws Exception {
        projectRepository.addNewGroup(projectCode, groupName);
    }

    @Override
    public void renameGroup(String projectCode, String oldGroupName, String newGroupName) throws Exception {
        projectRepository.renameGroup(projectCode, oldGroupName, newGroupName);
    }

    @Override
    public void updateBeforeAfterScenariosSettings(String projectCode, String oldPath, String newPath) {
        Project project = projectRepository.findProject(projectCode);
        if (StringUtils.equals(oldPath, newPath)) {
            return;
        }
        boolean updated = false;
        if (StringUtils.equals(project.getBeforeScenarioPath(), oldPath)) {
            project.setBeforeScenarioPath(newPath);
            updated = true;
        }
        if (StringUtils.equals(project.getAfterScenarioPath(), oldPath)) {
            project.setAfterScenarioPath(newPath);
            updated = true;
        }
        if (updated) {
            projectRepository.saveProject(project);
        }
    }
}
