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

package ru.bsc.test.autotester.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.beans.factory.annotation.Autowired;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.autotester.component.JsonDiffCalculator;
import ru.bsc.test.autotester.model.ExecutionResult;
import ru.bsc.test.autotester.ro.ExecutionResultRo;
import ru.bsc.test.autotester.ro.ScenarioResultRo;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Mapper(config = Config.class)
public abstract class ExecutionResultRoMapper {

    @Autowired
    private JsonDiffCalculator diffCalculator;
    @Autowired
    private ProjectRoMapper projectRoMapper;
    @Autowired
    private StepRoMapper stepRoMapper;

    @Mappings({
            @Mapping(target = "finished", source = "finished"),
            @Mapping(target = "scenarioResultList", ignore = true),
    })

    /* default */ abstract ExecutionResultRo executionResultToRo(ExecutionResult executionResult);

    public ExecutionResultRo map(ExecutionResult executionResult) {
        ExecutionResultRo executionResultRo = executionResultToRo(executionResult);
        if (executionResultRo == null) {
            return null;
        }

        if (executionResult != null) {
            List<ScenarioResultRo> scenarioResultList = executionResult.getScenarioResults()
                    .stream()
                    .map(scenarioListEntry -> ScenarioResultRo.builder()
                            .scenario(projectRoMapper.scenarioToScenarioRo("", scenarioListEntry.getScenario()))
                            .stepResultList(stepRoMapper.convertStepResultListToStepResultRo(scenarioListEntry.getStepResultList()))
                            .totalSteps(scenarioListEntry
                                    .getScenario()
                                    .getStepList()
                                    .stream()
                                    .filter(step -> !step.getDisabled())
                                    .map(Step::getStepParameterSetList)
                                    .mapToInt(list -> list != null ? (list.size() == 0 ? 1 : list.size()) : 1)
                                    .sum()
                            ).build()
                    )
                    .collect(Collectors.toList());
            executionResultRo.setScenarioResultList(scenarioResultList);
        }

        addDiffToResult(executionResultRo);
        return executionResultRo;
    }

    private void addDiffToResult(ExecutionResultRo executionResultRo) {
        if (executionResultRo.getScenarioResultList() == null) {
            return;
        }
        executionResultRo.getScenarioResultList().stream()
                .map(ScenarioResultRo::getStepResultList)
                .flatMap(List::stream)
                .forEach(result -> result.setDiff(diffCalculator.calculate(result.getActual(), result.getExpected())));
    }
}
