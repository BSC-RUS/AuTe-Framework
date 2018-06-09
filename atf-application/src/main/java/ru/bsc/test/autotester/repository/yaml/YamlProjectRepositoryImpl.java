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

package ru.bsc.test.autotester.repository.yaml;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import ru.bsc.test.at.executor.model.AmqpBroker;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.Stand;
import ru.bsc.test.at.util.YamlUtils;
import ru.bsc.test.autotester.component.Translator;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.properties.StandProperties;
import ru.bsc.test.autotester.repository.ProjectRepository;
import ru.bsc.test.autotester.repository.yaml.base.BaseYamlRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sdoroshin on 27.10.2017.
 *
 */
@Repository
@Slf4j
public class YamlProjectRepositoryImpl extends BaseYamlRepository implements ProjectRepository {
    private final EnvironmentProperties environmentProperties;

    @Autowired
    public YamlProjectRepositoryImpl(
            EnvironmentProperties environmentProperties,
            Translator translator
    ) {
        super(translator);
        this.environmentProperties = environmentProperties;
    }

    @Override
    public List<Project> findAllProjects() {
        List<Project> projectList = new LinkedList<>();
        log.debug("Load projects from: {}", environmentProperties.getProjectsDirectoryPath());
        File[] fileList = new File(environmentProperties.getProjectsDirectoryPath()).listFiles(File::isDirectory);
        if (fileList != null) {
            for (File projectDirectory : fileList) {
                log.debug("Reading directory: {}", projectDirectory.getAbsolutePath());
                Project loadedProject = loadProject(projectDirectory);
                if (loadedProject != null) {
                    projectList.add(loadedProject);
                }
            }
        }

        return projectList;
    }

    @Override
    public synchronized List<Project> findAllProjectsWithScenarios() {
        List<Project> projectList = new LinkedList<>();

        projectList.clear();
        log.debug("Load projects from: {}", environmentProperties.getProjectsDirectoryPath());
        File[] fileList = new File(environmentProperties.getProjectsDirectoryPath()).listFiles(File::isDirectory);
        if (fileList != null) {
            for (File projectDirectory : fileList) {
                log.debug("Reading directory: {}", projectDirectory.getAbsolutePath());
                File mainYml = new File(projectDirectory, MAIN_YML_FILENAME);
                if (!mainYml.exists()) {
                    continue;
                }

                try {
                    Project loadedProject = YamlUtils.loadAs(mainYml, Project.class);
                    loadedProject.setCode(projectDirectory.getName());
                    environmentToProject(loadedProject);
                    readExternalFiles(loadedProject);

                    projectList.add(loadedProject);
                } catch (IOException e) {
                    log.error("Main project file not found", e);
                }
            }
        }

        return projectList;
    }

    @Override
    public Project findProject(String projectCode) {
        if (StringUtils.isNotBlank(projectCode)) {
            File projectFile = Paths.get(environmentProperties.getProjectsDirectoryPath(), projectCode).toFile();
            log.debug("Load project from: {}", projectFile);
            if (projectFile.exists()) {
                return loadProject(projectFile);
            }
        }
        return null;
    }

    @Override
    public void saveProject(Project project) {
        Path path = Paths.get(environmentProperties.getProjectsDirectoryPath(), project.getCode(), MAIN_YML_FILENAME);
        try {
            clearProjectBeforeSave(project);
            removeGroups(project);
            YamlUtils.dumpToFile(project, path.toString());
        } catch (Exception e) {
            log.error("Save file {}", path, e);
        }
    }

    private void removeGroups(Project project) throws Exception{
        File file = Paths.get(
                environmentProperties.getProjectsDirectoryPath(),
                project.getCode()).toFile();
        Set<String> readGroups = new HashSet<>(readGroups(file));
        Set<String> groupSet = new HashSet<>(project.getGroupList() == null ? new ArrayList<>() : project.getGroupList());
        List<String> removed = readGroups.stream().filter(group -> !groupSet.contains(group)).collect(Collectors.toList());
        for(String group : removed){
            FileUtils.deleteDirectory(Paths.get(
                    environmentProperties.getProjectsDirectoryPath(),
                    project.getCode(),
                    "scenarios",
                    group).toFile());
            project.getGroupList().remove(group);

        }
    }

    @Override
    public void addNewGroup(String projectCode, String groupName) throws Exception {
        File file = Paths.get(
                environmentProperties.getProjectsDirectoryPath(),
                projectCode,
                "scenarios",
                groupName
        ).toFile();
        if (file.exists()) {
            throw new Exception("Directory already exists");
        } else {
            if (!file.mkdirs()) {
                throw new Exception("Directory not created");
            }
        }
    }

    @Override
    public void renameGroup(String projectCode, String oldGroupName, String newGroupName) throws Exception {
        File file = Paths.get(
                environmentProperties.getProjectsDirectoryPath(),
                projectCode,
                "scenarios",
                oldGroupName
        ).toFile();
        if (new File(file, SCENARIO_YML_FILENAME).exists() || !file.isDirectory()) {
            throw new Exception("This is not a group");
        } else {
            File newGroupDirectory = Paths.get(
                    environmentProperties.getProjectsDirectoryPath(),
                    projectCode,
                    "scenarios",
                    newGroupName
            ).toFile();
            if (!file.renameTo(newGroupDirectory)) {
                throw new Exception("Directory not renamed");
            }
        }
    }

    private Project loadProject(File directory) {
        File mainYml = new File(directory, MAIN_YML_FILENAME);
        if (!mainYml.exists()) {
            return null;
        }
        try {
            Project loadedProject = YamlUtils.loadAs(mainYml, Project.class);
            loadedProject.setCode(directory.getName());
            environmentToProject(loadedProject);
            loadGroups(loadedProject, directory);
            return loadedProject;
        } catch (IOException e) {
            log.error("Main project file not found", e);
        }
        return null;
    }

    private void environmentToProject(Project project) {
        StandProperties standProperties = environmentProperties.getProjectStandMap().get(project.getCode());
        if (standProperties != null) {
            Stand stand = new Stand();
            stand.setServiceUrl(standProperties.getServiceUrl());
            stand.setWireMockUrl(standProperties.getWireMockUrl());
            if (standProperties.getDataBase() != null) {
                stand.setDbUrl(standProperties.getDataBase().getUrl());
                stand.setDbUser(standProperties.getDataBase().getUser());
                stand.setDbPassword(standProperties.getDataBase().getPassword());
            }
            project.setStand(stand);

            if (standProperties.getAmqpBroker() != null) {
                AmqpBroker amqpBroker = new AmqpBroker();
                amqpBroker.setMqService(standProperties.getAmqpBroker().getMqService());
                amqpBroker.setHost(standProperties.getAmqpBroker().getHost());
                amqpBroker.setPort(standProperties.getAmqpBroker().getPort());
                amqpBroker.setUsername(standProperties.getAmqpBroker().getUsername());
                amqpBroker.setPassword(standProperties.getAmqpBroker().getPassword());
                project.setAmqpBroker(amqpBroker);
            }
        }
        project.setMqCheckCount(environmentProperties.getMqCheckCount());
        project.setMqCheckInterval(environmentProperties.getMqCheckInterval());
    }

    private void loadGroups(Project project, File projectDirectory) {
        project.setGroupList(readGroups(projectDirectory));
    }

    private List<String> readGroups(File projectDirectory){
        File scenariosDirectory = new File(projectDirectory, "scenarios");
        if (!scenariosDirectory.exists()) {
            return new ArrayList<>();
        }
        File[] files = scenariosDirectory.listFiles(File::isDirectory);
        if (files == null) {
            return new ArrayList<>();
        }
        List<String> groups = new ArrayList<>();
        for (File directory : files) {
            File scenarioYml = new File(directory, SCENARIO_YML_FILENAME);
            if (!scenarioYml.exists() && !groups.contains(directory.getName())) {
                groups.add(directory.getName());
            }
        }
        return groups;
    }

    private void clearProjectBeforeSave(Project project) {
        project.setScenarioList(null);
        project.setStand(null);
        project.setAmqpBroker(null);
    }

    private void readExternalFiles(Project project) {
        Set<String> groupSet = new HashSet<>();
        File file = Paths.get(
                environmentProperties.getProjectsDirectoryPath(),
                project.getCode(),
                "scenarios"
        ).toFile();
        File[] fileList = file.listFiles(File::isDirectory);
        if (fileList != null) {
            for (File directory : fileList) {
                File scenarioYml = new File(directory, SCENARIO_YML_FILENAME);
                if (scenarioYml.exists()) {
                    try {
                        project.getScenarioList().add(loadScenarioFromFiles(directory, null));
                    } catch (IOException e) {
                        log.error("Read file {}", scenarioYml.getAbsolutePath(), e);
                    }
                } else {
                    File[] innerFileList = directory.listFiles(File::isDirectory);
                    if (innerFileList != null) {
                        for (File scenarioYmlInGroup : innerFileList) {
                            if (new File(scenarioYmlInGroup, SCENARIO_YML_FILENAME).exists()) {
                                try {
                                    project.getScenarioList().add(loadScenarioFromFiles(
                                            scenarioYmlInGroup,
                                            directory.getName()
                                    ));
                                } catch (IOException e) {
                                    log.error("Read file {}", scenarioYmlInGroup, e);
                                }
                            }
                        }
                    }
                    groupSet.add(directory.getName());
                }
            }
        }
        List<String> groupList = new ArrayList<>(groupSet);
        Collections.sort(groupList);
        project.setGroupList(groupList);
    }

    private Scenario loadScenarioFromFiles(File scenarioDirectory, String group) throws IOException {
        File scenarioFile = new File(scenarioDirectory, SCENARIO_YML_FILENAME);
        Scenario scenario = YamlUtils.loadAs(scenarioFile, Scenario.class);
        scenario.setCode(scenarioFile.getParentFile().getName());
        scenario.setScenarioGroup(group);

        File scenarioRootDirectory = scenarioFile.getParentFile();
        scenario.getStepList().forEach(step ->
                loadStepFromFiles(step, scenarioRootDirectory)
        );

        return scenario;
    }
}
