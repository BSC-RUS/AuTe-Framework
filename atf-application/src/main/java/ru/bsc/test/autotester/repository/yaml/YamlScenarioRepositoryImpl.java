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

import static org.apache.commons.lang3.StringUtils.containsIgnoreCase;

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
        return loadScenarioFromFiles(
                Paths.get(projectsPath, projectCode, "scenarios", scenarioPath).toFile(),
                pathParts.length > 1 ? pathParts[0] : null,
                true
        );
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
                if (Paths.get(projectsPath, projectCode, "scenarios", newScenarioPath).toFile().exists()) {
                    throw new IOException("Directory already exists");
                }
            }

            Path path = Paths.get(projectsPath, projectCode, "scenarios", scenarioPath);
            if (Files.exists(path)) {
                File renamed = new File(path.toFile().getParentFile(), data.getCode() + "-" + UUID.randomUUID().toString());
                boolean canRemove = path.toFile().renameTo(renamed);
                if (canRemove) {
                    FileUtils.deleteDirectory(renamed);
                } else {
                    throw new IOException("Old scenario directory not removed");
                }
            }
        } else {
            if (Paths.get(projectsPath, projectCode, "scenarios", newScenarioPath).toFile().exists()) {
                throw new IOException("Directory already exists");
            }
        }

        data.setCode(newCode);
        File scenarioFile = Paths.get(
                projectsPath,
                projectCode,
                "scenarios",
                newScenarioPath,
                SCENARIO_YML_FILENAME
        ).toFile();

        log.info("scenarioFile: {}", scenarioFile);

        File scenarioRootDirectory = scenarioFile.getParentFile();
        saveScenarioToFiles(data, scenarioFile);
        data.getStepList().forEach(step -> loadStepFromFiles(step, scenarioRootDirectory));
        return data;
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
        Path scenarioDirectory = Paths.get(projectsPath, projectCode, "scenarios", scenarioPath);
        try (Stream<Path> filesStream = Files.walk(scenarioDirectory, FileVisitOption.FOLLOW_LINKS)) {
            filesStream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
        }
    }

    private List<Scenario> findScenarios(String projectCode, boolean fetchSteps) {
        File scenariosDirectory = Paths.get(projectsPath, projectCode, "scenarios").toFile();
        if (!scenariosDirectory.exists()) {
            return Collections.emptyList();
        }
        File[] directories = scenariosDirectory.listFiles(File::isDirectory);
        if (directories == null) {
            return Collections.emptyList();
        }
        List<Scenario> scenarios = new ArrayList<>();
        for (File directory : directories) {
            File scenarioYml = new File(directory, SCENARIO_YML_FILENAME);
            if (scenarioYml.exists()) {
                try {
                    scenarios.add(loadScenarioFromFiles(directory, null, fetchSteps));
                } catch (IOException e) {
                    log.error("Read file " + scenarioYml.getAbsolutePath(), e);
                }
            } else {
                File[] innerFileList = directory.listFiles(File::isDirectory);
                if (innerFileList != null) {
                    for (File scenarioYmlInGroup : innerFileList) {
                        if (new File(scenarioYmlInGroup, SCENARIO_YML_FILENAME).exists()) {
                            try {
                                scenarios.add(loadScenarioFromFiles(scenarioYmlInGroup, directory.getName(), fetchSteps));
                            } catch (IOException | ReaderException e) {
                                log.error("Read file {} {}", scenarioYmlInGroup, e);
                            }
                        }
                    }
                }
            }
        }
        return scenarios;
    }

    private boolean checkSteps(Scenario scenario, String relativeUrl) {
        return scenario.getStepList()
                .stream()
                .anyMatch(s -> containsIgnoreCase(s.getRelativeUrl(), relativeUrl));
    }

}
