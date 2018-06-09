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

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.bsc.test.at.executor.model.*;
import ru.bsc.test.autotester.exception.ResourceNotFoundException;
import ru.bsc.test.autotester.launcher.api.ScenarioLauncher;
import ru.bsc.test.autotester.mapper.ProjectRoMapper;
import ru.bsc.test.autotester.mapper.ScenarioRoMapper;
import ru.bsc.test.autotester.mapper.StepRoMapper;
import ru.bsc.test.autotester.model.ExecutionResult;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.report.AbstractReportGenerator;
import ru.bsc.test.autotester.repository.ScenarioRepository;
import ru.bsc.test.autotester.ro.*;
import ru.bsc.test.autotester.service.ProjectService;
import ru.bsc.test.autotester.service.ScenarioService;
import ru.bsc.test.autotester.utils.ZipUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.zip.ZipOutputStream;

/**
 * Created by sdoroshin on 21.03.2017.
 */
@Service
@Slf4j
public class ScenarioServiceImpl implements ScenarioService {
    private static final String DEFAULT_GROUP = "__default";
    //@formatter:off
    private static final TypeReference<List<StepResult>> RESULT_LIST_TYPE = new TypeReference<List<StepResult>>(){};
    //@formatter:on
    private final ConcurrentMap<String, ExecutionResult> runningScriptsMap = new ConcurrentHashMap<>();
    private final Set<String> stopExecutingSet = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private final ObjectMapper objectMapper = new ObjectMapper().setVisibility(
            PropertyAccessor.FIELD,
            JsonAutoDetect.Visibility.ANY
    );
    private final StepRoMapper stepRoMapper;
    private final ScenarioRoMapper scenarioRoMapper;
    private final ProjectRoMapper projectRoMapper;
    private final ScenarioRepository scenarioRepository;
    private final ProjectService projectService;
    private final EnvironmentProperties environmentProperties;
    private final AbstractReportGenerator reportGenerator;
    private final ScenarioLauncher scenarioLauncher;

    @Autowired
    public ScenarioServiceImpl(
            StepRoMapper stepRoMapper,
            ScenarioRoMapper scenarioRoMapper,
            ProjectRoMapper projectRoMapper,
            ScenarioRepository scenarioRepository,
            ProjectService projectService,
            EnvironmentProperties environmentProperties,
            AbstractReportGenerator reportGenerator,
            ScenarioLauncher scenarioLauncher
    ) {
        this.stepRoMapper = stepRoMapper;
        this.scenarioRoMapper = scenarioRoMapper;
        this.projectRoMapper = projectRoMapper;
        this.scenarioRepository = scenarioRepository;
        this.projectService = projectService;
        this.environmentProperties = environmentProperties;
        this.reportGenerator = reportGenerator;
        this.scenarioLauncher = scenarioLauncher;
    }

    @Override
    public StartScenarioInfoRo startScenarioExecutingList(Project project, List<Scenario> scenarioList) {
        loadBeforeAndAfterScenarios(project, scenarioList);
        StartScenarioInfoRo startScenarioInfoRo = new StartScenarioInfoRo();

        ExecutionResult executionResult = new ExecutionResult();
        final List<ScenarioResult> scenarioResultList = new ArrayList<>();
        executionResult.setScenarioResults(scenarioResultList);
        final String runningUuid = UUID.randomUUID().toString();
        startScenarioInfoRo.setRunningUuid(runningUuid);
        runningScriptsMap.put(runningUuid, executionResult);

        scenarioLauncher.launchScenarioFromUI(scenarioList, project, environmentProperties, executionResult, stopExecutingSet, projectService, runningUuid );
        return startScenarioInfoRo;
    }



    @Override
    public void stopExecuting(String executingUuid) {
        stopExecutingSet.add(executingUuid);
    }

    @Override
    public List<String> getExecutingList() {
        return new LinkedList<>(runningScriptsMap.keySet());
    }

    @Override
    public ExecutionResult getResult(String executingUuid) {
        return runningScriptsMap.get(executingUuid);
    }

    @Override
    public List<StepResult> getResult(ScenarioIdentityRo identity) {
        try {
            return loadResults(identity);
        } catch (IOException e) {
            log.error("Error while loading scenario", e);
        }
        return null;
    }

    @Override
    public void getReport(List<ScenarioIdentityRo> identities, ZipOutputStream outputStream) throws Exception {
        log.info("Scenario identities to generate report: {}", identities.size());
        reportGenerator.clear();
        identities.forEach(identity -> {
            try {
                List<StepResult> results = loadResults(identity);
                if (results != null) {
                    String scenarioGroup = identity.getGroup();
                    String scenarioPath = (StringUtils.isEmpty(scenarioGroup) ? "" : scenarioGroup + "/") + identity.getCode();
                    Scenario scenario = scenarioRepository.findScenario(identity.getProjectCode(), scenarioPath);
                    reportGenerator.add(scenario, results);
                }
            } catch (IOException e) {
                log.error("Error while loading scenario", e);
            }
        });
        if (reportGenerator.isEmpty()) {
            throw new ResourceNotFoundException();
        }

        File tmpDirectory = new File("tmp", UUID.randomUUID().toString());
        if (tmpDirectory.mkdirs()) {
            reportGenerator.generate(tmpDirectory);
            ZipUtils.pack(tmpDirectory, outputStream);
        } else {
            throw new FileAlreadyExistsException(tmpDirectory.getAbsolutePath());
        }
    }

    @Override
    public StepRo addStepToScenario(String projectCode, String scenarioPath, StepRo stepRo) throws IOException {
        synchronized (projectService) {
            Scenario scenario = findOne(projectCode, scenarioPath);
            if (scenario != null) {
                Step newStep = stepRoMapper.convertStepRoToStep(stepRo);
                scenario.getStepList().add(newStep);
                scenarioRepository.saveScenario(projectCode, scenarioPath, scenario, false);
                return stepRoMapper.stepToStepRo(newStep);
            }
            return null;
        }
    }

    @Override
    public Scenario saveScenario(String projectCode, String scenarioPath, Scenario scenario) throws IOException {
        return scenarioRepository.saveScenario(projectCode, scenarioPath, scenario, false);
    }

    @Override
    public void deleteOne(String projectCode, String scenarioPath) throws IOException {
        synchronized (projectService) {
            scenarioRepository.delete(projectCode, scenarioPath);
        }
    }

    @Override
    public ScenarioRo updateScenarioFormRo(String projectCode, String scenarioPath, ScenarioRo scenarioRo) throws IOException {
        synchronized (projectService) {
            Scenario scenario = scenarioRepository.findScenario(projectCode, scenarioPath);
            if (scenario != null) {
                String oldPath = scenario.getPath();
                scenario = scenarioRoMapper.updateScenario(scenarioRo, scenario);
                scenario = scenarioRepository.saveScenario(projectCode, scenarioPath, scenario, true);
                String newPath = scenario.getPath();
                projectService.updateBeforeAfterScenariosSettings(projectCode, oldPath, newPath);
                return projectRoMapper.scenarioToScenarioRo(projectCode, scenario);
            }
            return null;
        }
    }

    @Override
    public Scenario findOne(String projectCode, String scenarioPath) throws IOException {
        synchronized (projectService) {
            return scenarioRepository.findScenario(projectCode, scenarioPath);
        }
    }

    @Override
    public Step cloneStep(String projectCode, String scenarioPath, String stepCode) throws IOException {
        synchronized (projectService) {
            Scenario scenario = scenarioRepository.findScenario(projectCode, scenarioPath);
            Step existsStep = scenario.getStepList().stream()
                    .filter(step -> Objects.equals(step.getCode(), stepCode))
                    .findAny()
                    .orElse(null);
            if (existsStep != null) {
                Step newStep = existsStep.copy();
                scenario.getStepList().add(newStep);
                return newStep;
            }
            return null;
        }
    }

    @Override
    public List<StepRo> updateStepListFromRo(String projectCode, String scenarioPath, List<StepRo> stepRoList) throws IOException {
        synchronized (projectService) {
            Scenario scenario = findOne(projectCode, scenarioPath);
            if (scenario != null) {
                stepRoMapper.updateScenarioStepList(stepRoList, scenario);
                scenario = scenarioRepository.saveScenario(projectCode, scenarioPath, scenario, false);
                return stepRoMapper.convertStepListToStepRoList(scenario.getStepList());
            }
            throw new ResourceNotFoundException();
        }
    }

    @Override
    public List<ScenarioRo> findScenarioByStepRelativeUrl(String projectCode, ProjectSearchRo projectSearchRo) {
        List<Scenario> scenarios = new ArrayList<>();
        if (!StringUtils.isEmpty(projectSearchRo.getRelativeUrl())) {
            scenarios = new ArrayList<>(scenarioRepository.findByRelativeUrl(projectCode, projectSearchRo.getRelativeUrl()));
        }
        return projectRoMapper.convertScenarioListToScenarioRoList(scenarios);
    }

    @Override
    public StepRo updateStepFromRo(String projectCode, String scenarioPath, String stepCode, StepRo stepRo) throws IOException {
        synchronized (projectService) {
            Scenario scenario = scenarioRepository.findScenario(projectCode, scenarioPath);
            Step existsStep = scenario.getStepList().stream()
                    .filter(step -> Objects.equals(step.getCode(), stepCode))
                    .findAny()
                    .orElse(null);
            stepRoMapper.updateStep(stepRo, existsStep);
            scenarioRepository.saveScenario(projectCode, scenarioPath, scenario, false);
            return stepRoMapper.stepToStepRo(existsStep);
        }
    }

    @Override
    public List<Scenario> findAllByProject(String projectCode) {
        return scenarioRepository.findScenarios(projectCode);
    }

    @Override
    public ScenarioRo addScenarioToProject(String projectCode, ScenarioRo scenarioRo) throws IOException {
        synchronized (this) {
            Scenario newScenario = scenarioRoMapper.updateScenario(scenarioRo, new Scenario());
            newScenario = scenarioRepository.saveScenario(projectCode, null, newScenario, true);
            return projectRoMapper.scenarioToScenarioRo(projectCode, newScenario);
        }
    }

    private void loadBeforeAndAfterScenarios(Project project, List<Scenario> scenarioList) {
        for (Scenario scenario : scenarioList) {
            try {
                if (!scenario.getBeforeScenarioIgnore() && project.getBeforeScenarioPath() != null) {
                    project.getScenarioList().add(scenarioRepository.findScenario(
                            project.getCode(),
                            project.getBeforeScenarioPath()
                    ));
                }
            } catch (IOException e) {
                log.error("Error while loading before scenario", e);
            }
            try {
                if (!scenario.getAfterScenarioIgnore() && project.getAfterScenarioPath() != null) {
                    project.getScenarioList().add(scenarioRepository.findScenario(
                            project.getCode(),
                            project.getAfterScenarioPath()
                    ));
                }
            } catch (IOException e) {
                log.error("Error while loading after scenario", e);
            }
        }
    }

    private List<StepResult> loadResults(ScenarioIdentityRo identity) throws IOException {
        String scenarioGroup = identity.getGroup();
        String groupDir = scenarioGroup != null ? scenarioGroup : DEFAULT_GROUP;
        Path resultsFile = Paths.get(
                "tmp",
                "results",
                identity.getProjectCode(),
                groupDir,
                identity.getCode(),
                "results.json"
        );
        List<StepResult> results = null;
        if (Files.exists(resultsFile)) {
            results = objectMapper.readValue(resultsFile.toFile(), RESULT_LIST_TYPE);
        }
        return results;
    }
}
