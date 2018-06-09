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

package ru.bsc.test.autotester.launcher.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.ScenarioResult;
import ru.bsc.test.autotester.launcher.api.ScenarioLauncher;
import ru.bsc.test.autotester.launcher.api.TestLauncher;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.report.AbstractReportGenerator;
import ru.bsc.test.autotester.repository.ProjectRepository;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TestLauncherImpl implements TestLauncher {
    private final EnvironmentProperties properties;
    private final ProjectRepository projectRepository;
    private final AbstractReportGenerator reportGenerator;
    private final ScenarioLauncher scenarioLauncher;

    public TestLauncherImpl(
            EnvironmentProperties properties,
            ProjectRepository projectRepository,
            AbstractReportGenerator reportGenerator,
            ScenarioLauncher scenarioLauncher
    ) {
        this.properties = properties;
        this.projectRepository = projectRepository;
        this.reportGenerator = reportGenerator;
        this.scenarioLauncher = scenarioLauncher;
    }

    @Override
    public void launch() throws Exception {
        log.info("Launch scenarios ...");
        List<Project> projectList = projectRepository.findAllProjectsWithScenarios();
        List<ScenarioResult> allProjectsResult = new ArrayList<>();

        for (Project project: projectList) {
            if (properties.getProjectStandMap() == null || !properties.getProjectStandMap().containsKey(project.getCode())) {
                continue;
            }
            log.info("Launch scenarios for project {}", project.getName());
            List<ScenarioResult> projectResults = scenarioLauncher.launchScenarioFromCLI(project.getScenarioList(), project, properties, reportGenerator);
            log.debug("Project run results {}", projectResults);
            allProjectsResult.addAll(projectResults);
        }
        LaunchResult launchResult = new LaunchResult(allProjectsResult);
        log.info("Launch result {}", launchResult);
        reportGenerator.generate(new File("." + File.separator + "report"));
        log.info("Report for test run generated");
        TestRunnerExitCodeContext.getInstance().setExitCode(launchResult.isFailed() ? 1 : 0);
    }
}
