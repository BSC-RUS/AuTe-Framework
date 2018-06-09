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

package ru.bsc.test.autotester.report.impl.allure;

import com.google.gson.Gson;
import io.qameta.allure.ConfigurationBuilder;
import io.qameta.allure.ReportGenerator;
import io.qameta.allure.allure1.Allure1Plugin;
import io.qameta.allure.allure2.Allure2Plugin;
import io.qameta.allure.category.CategoriesPlugin;
import io.qameta.allure.context.FreemarkerContext;
import io.qameta.allure.context.JacksonContext;
import io.qameta.allure.context.MarkdownContext;
import io.qameta.allure.context.RandomUidContext;
import io.qameta.allure.core.*;
import io.qameta.allure.duration.DurationPlugin;
import io.qameta.allure.duration.DurationTrendPlugin;
import io.qameta.allure.environment.Allure1EnvironmentPlugin;
import io.qameta.allure.executor.ExecutorPlugin;
import io.qameta.allure.history.HistoryPlugin;
import io.qameta.allure.history.HistoryTrendPlugin;
import io.qameta.allure.launch.LaunchPlugin;
import io.qameta.allure.owner.OwnerPlugin;
import io.qameta.allure.plugin.DefaultPluginLoader;
import io.qameta.allure.severity.SeverityPlugin;
import io.qameta.allure.status.StatusChartPlugin;
import io.qameta.allure.suites.SuitesPlugin;
import io.qameta.allure.summary.SummaryPlugin;
import io.qameta.allure.tags.TagsPlugin;
import io.qameta.allure.timeline.TimelinePlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.executor.model.RequestData;
import ru.bsc.test.at.executor.model.Scenario;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.autotester.report.AbstractReportGenerator;
import ru.bsc.test.autotester.report.impl.allure.attach.builder.AttachBuilder;
import ru.bsc.test.autotester.report.impl.allure.plugin.DefaultCategoriesPlugin;
import ru.bsc.test.autotester.report.impl.allure.plugin.HistoryRestorePlugin;
import ru.bsc.test.autotester.report.impl.allure.plugin.ProjectContext;
import ru.yandex.qatools.allure.model.Status;
import ru.yandex.qatools.allure.model.Step;
import ru.yandex.qatools.allure.model.TestCaseResult;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.collections.CollectionUtils.isNotEmpty;
import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static ru.yandex.qatools.allure.model.Status.FAILED;
import static ru.yandex.qatools.allure.model.Status.PASSED;

@Slf4j
@Component
public class AllureReportGenerator extends AbstractReportGenerator {
    private static final String WITHOUT_GROUP = "Без группы";
    private static final String PLUGINS_DIRECTORY = "allure-plugins";

    private final Gson gson = new Gson();
    private final AttachBuilder<StepResult> stepResultAttachBuilder;
    private final AttachBuilder<RequestData> requestDataAttachBuilder;
    private final ReportGenerator generator;
    private final Configuration configuration;
    private final HistoryFilesProcessor historyFilesProcessor;

    @Autowired
    public AllureReportGenerator(
            AttachBuilder<StepResult> stepResultAttachBuilder,
            AttachBuilder<RequestData> requestDataAttachBuilder,
            HistoryFilesProcessor historyFilesProcessor
    ) {
        this.stepResultAttachBuilder = stepResultAttachBuilder;
        this.requestDataAttachBuilder = requestDataAttachBuilder;
        this.historyFilesProcessor = historyFilesProcessor;
        this.configuration = createConfiguration();
        this.generator = new ReportGenerator(configuration);
    }

    @Override
    public synchronized void generate(File directory) throws Exception {
        File resultDirectory = new File(directory, "results-directory");
        log.info("Generate allure report to: {}", directory);
        log.info("Scenario step results map size: {}", getScenarioStepResultMap().size());
        if (!resultDirectory.exists() && !resultDirectory.mkdirs()) {
            throw new Exception("mkdirs failed: " + resultDirectory.getAbsolutePath());
        }

        for (AllurePreparedData data : buildReportData(resultDirectory, getScenarioStepResultMap())) {
            try (FileWriter writer = new FileWriter(data.getDataFile())) {
                gson.toJson(data.getSuiteResult(), writer);
            } catch (IOException e) {
                log.error("Could not convert testSuiteResult {} to json", data, e);
            }
        }

        ProjectContext projectContext = configuration.requireContext(ProjectContext.class);
        String projectCode = detectProjectCode();
        projectContext.setProjectCode(projectCode);
        Path output = new File(directory + File.separator + "output").toPath();
        final Path resultsDirectory = resultDirectory.toPath();
        generator.generate(output, resultsDirectory);
        historyFilesProcessor.process(projectCode, output);
        FileUtils.deleteDirectory(resultDirectory);
        log.info("Allure report successfully generated: {}", directory);
    }

    private Configuration createConfiguration() {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder().fromExtensions(Arrays.asList(
                new JacksonContext(),
                new MarkdownContext(),
                new FreemarkerContext(),
                new RandomUidContext(),
                new ProjectContext(),
                new MarkdownDescriptionsPlugin(),
                new TagsPlugin(),
                new SeverityPlugin(),
                new OwnerPlugin(),
                new DefaultCategoriesPlugin(),
                new CategoriesPlugin(),
                new HistoryRestorePlugin(),
                new HistoryPlugin(),
                new HistoryTrendPlugin(),
                new DurationPlugin(),
                new DurationTrendPlugin(),
                new StatusChartPlugin(),
                new TimelinePlugin(),
                new SuitesPlugin(),
                new ReportWebPlugin(),
                new TestsResultsPlugin(),
                new AttachmentsPlugin(),
                new SummaryPlugin(),
                new ExecutorPlugin(),
                new LaunchPlugin(),
                new Allure1Plugin(),
                new Allure1EnvironmentPlugin(),
                new Allure2Plugin()
        ));
        List<Plugin> plugins = loadPlugins();
        if (!plugins.isEmpty()) {
            configurationBuilder = configurationBuilder.fromPlugins(plugins);
        }
        return configurationBuilder.build();
    }

    private List<Plugin> loadPlugins() {
        log.info("Loading allure plugins");
        Path pluginsPath = Paths.get(PLUGINS_DIRECTORY);
        if (Files.exists(pluginsPath) && Files.isDirectory(pluginsPath)) {
            final DefaultPluginLoader pluginLoader = new DefaultPluginLoader();
            final ClassLoader classLoader = getClass().getClassLoader();
            try (Stream<Path> filesStream = Files.list(pluginsPath);) {
                List<Plugin> plugins = filesStream
                        .filter(Files::isDirectory)
                        .map(pluginDirectory -> pluginLoader.loadPlugin(classLoader, pluginDirectory))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
                log.info("Plugins loaded: {}", plugins.size());
                return plugins;
            } catch (IOException e) {
                log.error("Exception while loading plugins", e);
            }
        }
        log.warn("Allure plugins directory not exists, plugins not loaded");
        return Collections.emptyList();
    }

    private String detectProjectCode() {
        List<StepResult> results = getScenarioStepResultMap().values().stream().findFirst().orElse(null);
        if (CollectionUtils.isEmpty(results)) {
            return null;
        }
        return results.get(0).getProjectCode();
    }

    private List<AllurePreparedData> buildReportData(
            File resultDirectory,
            Map<Scenario, List<StepResult>> scenarioStepResultMap
    ) {
        Set<String> groups = scenarioStepResultMap.keySet().stream()
                .map(Scenario::getScenarioGroup)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        groups.add(WITHOUT_GROUP);
        return groups.stream()
                .map(group -> buildTestSuiteData(resultDirectory, group, scenarioStepResultMap))
                .collect(Collectors.toList());
    }

    private AllurePreparedData buildTestSuiteData(
            File resultDirectory,
            String group,
            Map<Scenario, List<StepResult>> scenarioListMap
    ) {
        File dataFile = new File(resultDirectory + File.separator + UUID.randomUUID() + "-testsuite.json");
        TestSuiteResult suiteResult = new TestSuiteResult()
                .withName(group)
                .withTestCases(buildTestCasesData(resultDirectory, group, scenarioListMap));
        return AllurePreparedData.of(dataFile, suiteResult);
    }

    private List<TestCaseResult> buildTestCasesData(
            File resultDirectory,
            String group,
            Map<Scenario, List<StepResult>> scenarioStepResultMap
    ) {
        return scenarioStepResultMap.entrySet().stream()
                .filter(entry -> group.equals(entry.getKey().getScenarioGroup() == null ?
                                              WITHOUT_GROUP :
                                              entry.getKey().getScenarioGroup()))
                .map(entry -> buildTestCaseData(resultDirectory, entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private TestCaseResult buildTestCaseData(File resultDirectory, Scenario scenario, List<StepResult> stepResults) {
        List<Step> steps = buildStepsData(resultDirectory, stepResults);
        return new TestCaseResult()
                .withName(scenario.getName())
                .withStart(steps.stream().mapToLong(Step::getStart).min().orElse(0))
                .withStop(steps.stream().mapToLong(Step::getStop).max().orElse(0))
                .withStatus(steps.stream().anyMatch(step -> FAILED.equals(step.getStatus())) ? FAILED : PASSED)
                .withSteps(steps);
    }

    private List<Step> buildStepsData(File resultDirectory, List<StepResult> stepResults) {
        return stepResults.stream()
                .collect(Collectors.groupingBy(StepResult::getStep))
                .entrySet()
                .stream()
                .map(buildSteps(resultDirectory))
                .flatMap(List::stream)
                .sorted(Comparator.comparingLong(Step::getStart))
                .collect(Collectors.toList());
    }

    private Function<Map.Entry<ru.bsc.test.at.executor.model.Step, List<StepResult>>, List<Step>> buildSteps(File resultDirectory) {
        return resultGroup -> Collections.singletonList(buildStep(
                resultDirectory,
                stepName(resultGroup.getKey()),
                resultGroup.getValue().get(0)
        ));
    }

    private Step buildStep(File resultDirectory, String name, StepResult result) {
        Step step = new Step()
                .withName(name)
                .withStart(result.getStart())
                .withStop(result.getStop())
                .withStatus(result.getResult().isPositive() ? PASSED : FAILED)
                .withAttachments(stepResultAttachBuilder.build(resultDirectory, result));
        List<RequestData> requestDataList = result.getRequestDataList();
        if (isNotEmpty(requestDataList) && requestDataList.size() > 1) {
            step = step.withSteps(buildStepsForCyclicRequest(resultDirectory, step.getStatus(), requestDataList));
        }
        return step;
    }

    private List<Step> buildStepsForCyclicRequest(
            File resultDirectory,
            Status status,
            List<RequestData> requestDataList
    ) {
        List<Step> steps = new ArrayList<>();
        for (int i = 0; i < requestDataList.size(); i++) {
            RequestData requestData = requestDataList.get(i);
            steps.add(new Step()
                    .withName("Request " + Integer.toString(i + 1))
                    .withStatus(status)
                    .withAttachments(requestDataAttachBuilder.build(resultDirectory, requestData)));
        }
        return steps;
    }

    private String stepName(ru.bsc.test.at.executor.model.Step step) {
        return isNotEmpty(step.getStepComment()) ? step.getStepComment() : step.getCode();
    }
}
