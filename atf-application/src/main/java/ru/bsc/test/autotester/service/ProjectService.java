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

package ru.bsc.test.autotester.service;

import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.autotester.ro.ProjectRo;

import java.io.IOException;
import java.util.List;

/**
 * Created by sdoroshin on 03.03.2017.
 *
 */
public interface ProjectService {

    List<Project> findAll();

    Project findOne(String projectCode);

    ProjectRo updateFromRo(String projectCode, ProjectRo projectRo);

    ProjectRo createFromRo(ProjectRo projectRo) throws IOException;

    void addNewGroup(String projectCode, String groupName) throws Exception;

    void renameGroup(String projectCode, String oldGroupName, String newGroupName) throws Exception;

    void updateBeforeAfterScenariosSettings(String projectCode, String oldPath, String newPath);
}
