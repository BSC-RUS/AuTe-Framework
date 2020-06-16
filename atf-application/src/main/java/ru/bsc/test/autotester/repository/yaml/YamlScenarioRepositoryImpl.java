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
import org.yaml.snakeyaml.reader.ReaderException;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.autotester.component.Translator;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.repository.ScenarioRepository;
import ru.bsc.test.autotester.repository.yaml.base.BaseYamlRepository;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;
import static ru.bsc.test.at.executor.utils.StreamUtils.nullSafeStream;

/**
 * Created by sdoroshin on 27.10.2017.
 *
 */

@Repository
@Slf4j
public class YamlScenarioRepositoryImpl extends BaseYamlRepository implements ScenarioRepository {

    private final String projectsPath;

    @Autowired
    public YamlScenarioRepositoryImpl(EnvironmentProperties environmentProperties, Translator translator) {
        super(translator);
        this.projectsPath = environmentProperties.getProjectsDirectoryPath();
    }

    @Override
    public List<Scenario> findScenarios(String projectCode) {
        return findScenarios(projectCode, false);
    }

    @Override
    public List<Scenario> findScenariosWithSteps(String projectCode) {
        return findScenarios(projectCode, true);
    }

    @Override
    public Scenario findScenario(String projectCode, String scenarioPath) throws IOException {
        String[] pathParts = scenarioPath.split("/");
        return loadScenarioFromFiles(getScenarioFolder(projectCode, scenarioPath), pathParts.length > 1 ? pathParts[0] : null, true);
    }

    @Override
    public Scenario saveScenario(String projectCode, String scenarioPath, Scenario data, boolean updateDirectoryName) throws IOException {
        if (StringUtils.isBlank(data.getName())) {
            throw new IOException("Empty scenario name");
        }

        String newCode = updateDirectoryName ? translator.translate(data.getName()) : data.getCode();
        log.info("newCode: {}", newCode);
        String newScenarioPath = getScenarioPath(data.getScenarioGroup(), newCode);
        log.info("newScenarioPath: {}", newScenarioPath);
        if (scenarioPath != null) {
            String[] pathParts = scenarioPath.split("/");
            String codePart = pathParts.length > 1 ? pathParts[1] : pathParts[0];
            String groupPart = pathParts.length > 1 ? pathParts[0] : null;
            String oldScenarioPath = getScenarioPath(groupPart, codePart);

            if (!Objects.equals(newScenarioPath, oldScenarioPath)) {
                if (getScenarioFolder(projectCode, newScenarioPath).exists()) {
                    throw new IOException("Directory already exists");
                }
            }

            File scenarioFolder = getScenarioFolder(projectCode, scenarioPath);
            if (scenarioFolder.exists()) {
                File renamed = new File(scenarioFolder.getParentFile(), data.getCode() + "-" + UUID.randomUUID().toString());
                boolean canRemove = scenarioFolder.renameTo(renamed);
                if (canRemove) {
                    FileUtils.deleteDirectory(renamed);
                } else {
                    throw new IOException("Old scenario directory not removed");
                }
            }
        } else {
            if (getScenarioFolder(projectCode, newScenarioPath).exists()) {
                throw new IOException("Directory already exists");
            }
        }

        data.setCode(newCode);
        checkExistingScenariosFolder(projectCode);
        File scenarioFile = getScenarioFile(projectCode, newScenarioPath);

        log.info("scenarioFile: {}", scenarioFile);

        File scenarioRootDirectory = scenarioFile.getParentFile();
        saveScenarioToFiles(data, scenarioFile);
        data.getStepList().forEach(step -> loadStepFromFiles(step, scenarioRootDirectory));
        return data;
    }

    private void checkExistingScenariosFolder(String projectCode) throws IOException {
        Path scenariosPath = Paths.get(projectsPath, projectCode, SCENARIOS_FOLDER_NAME);
        if (Files.notExists(scenariosPath)) {
            Files.createDirectory(scenariosPath);
        }
    }

    private Path getScenarioFolderPath(String projectCode, String scenarioPath) {
        return Paths.get(projectsPath, projectCode, SCENARIOS_FOLDER_NAME, scenarioPath);
    }

    private File getScenarioFolder(String projectCode, String scenarioPath) {
        return getScenarioFolderPath(projectCode, scenarioPath).toFile();
    }

    private File getScenarioFile(String projectCode, String scenarioPath) {
        return Paths.get(projectsPath, projectCode, SCENARIOS_FOLDER_NAME, scenarioPath, SCENARIO_YML_FILENAME).toFile();
    }

    private String getScenarioPath(String scenarioGroup, String code) {
        return StringUtils.isNotEmpty(scenarioGroup) ? scenarioGroup + "/" + code : code;
    }

    @Override
    public Set<Scenario> findByRelativeUrl(String projectCode, String relativeUrl) {
        return findScenariosWithSteps(projectCode).stream()
                .filter(scenario -> checkSteps(scenario, relativeUrl))
                .collect(Collectors.toSet());
    }

    @Override
    public void delete(String projectCode, String scenarioPath) throws IOException {
        try (Stream<Path> filesStream = Files.walk(getScenarioFolderPath(projectCode, scenarioPath), FileVisitOption.FOLLOW_LINKS)) {
            filesStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private List<Scenario> findScenarios(String projectCode, boolean fetchSteps) {
        File scenariosDirectory = Paths.get(projectsPath, projectCode, SCENARIOS_FOLDER_NAME).toFile();
        if (!scenariosDirectory.exists()) {
            return Collections.emptyList();
        }
        File[] directories = scenariosDirectory.listFiles(File::isDirectory);

        List<Scenario> scenarios = new ArrayList<>();
        nullSafeStream(directories).forEach(directory -> {
            File scenarioYml = new File(directory, SCENARIO_YML_FILENAME);
            if (scenarioYml.exists()) {
                if(scenarioYml.length() > 0) {
                    try {
                        scenarios.add(loadScenarioFromFiles(directory, null, fetchSteps));
                    } catch(IOException e) {
                        log.error("Read file " + scenarioYml.getAbsolutePath(), e);
                    }
                } else {
                    log.warn("Scenario file {} is empty and will be ignored", scenarioYml.getAbsolutePath());
                }
            } else {
                File[] innerDirectories = directory.listFiles(File::isDirectory);
                nullSafeStream(innerDirectories)
                    .filter(this::scenarioYmlFileExist)
                    .forEach(scenarioYmlInGroup -> {
                        File scenarioFile = new File(scenarioYmlInGroup, SCENARIO_YML_FILENAME);
                        if(scenarioFile.length() > 0) {
                            try {
                                scenarios.add(loadScenarioFromFiles(scenarioYmlInGroup, directory.getName(), fetchSteps));
                            } catch (IOException | ReaderException e) {
                                log.error("Read file {} {}", scenarioYmlInGroup, e);
                            }
                        } else {
                            log.warn("Scenario file {} is empty and will be ignored", scenarioFile.getAbsolutePath());
                        }
                    });
            }
        });

        return scenarios.stream()
            .sorted(comparing(Scenario::getName, String.CASE_INSENSITIVE_ORDER))
            .collect(toList());
    }


    private boolean scenarioYmlFileExist(File directory) {
        return new File(directory, SCENARIO_YML_FILENAME).exists();
    }

    private boolean checkSteps(Scenario scenario, String relativeUrl) {
        return scenario.getStepList()
                .stream()
                .anyMatch(s -> containsIgnoreCase(s.getRelativeUrl(), relativeUrl));
    }

}
