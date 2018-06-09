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

package ru.bsc.test.at.executor.model;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by sdoroshin on 10.05.2017.
 */
@Getter
@Setter
public class Project implements Serializable, AbstractModel {
    private static final long serialVersionUID = 7331632683933716938L;

    private String code;
    private String name;
    private List<Scenario> scenarioList = new LinkedList<>();
    private String beforeScenarioPath;
    private String afterScenarioPath;
    private Stand stand;
    private Boolean useRandomTestId;
    private String testIdHeaderName;
    private AmqpBroker amqpBroker;
    private List<String> groupList;
    private Long mqCheckInterval;
    private Integer mqCheckCount;

    public Project copy() {
        Project project = new Project();
        project.setName(getName());
        project.setCode(getCode() + "_COPY");
        project.setUseRandomTestId(getUseRandomTestId());
        project.setTestIdHeaderName(getTestIdHeaderName());
        project.setStand(getStand().copy());
        if (getScenarioList() != null) {
            project.setScenarioList(new LinkedList<>());
            for (Scenario scenario : getScenarioList()) {
                Scenario projectScenario = scenario.copy();
                projectScenario.setScenarioGroup(scenario.getScenarioGroup());
                project.getScenarioList().add(projectScenario);
            }
        }

        if (getGroupList() != null) {
            project.setGroupList(new LinkedList<>());
            for (String group : getGroupList()) {
                project.getGroupList().add(group);
            }
        }

        project.setAmqpBroker(getAmqpBroker().copy());
        return project;
    }

    @Override
    public String toString() {
        return "Project{" + code + '}';
    }

    public Boolean getUseRandomTestId() {
        return useRandomTestId != null && useRandomTestId;
    }
}
