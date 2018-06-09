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

import lombok.Getter;
import lombok.Setter;
import ru.yandex.qatools.allure.model.TestSuiteResult;

import java.io.File;

/**
 * Created by smakarov
 * 21.03.2018 12:04
 */
@Getter
@Setter
class AllurePreparedData {
    private File dataFile;
    private TestSuiteResult suiteResult;

    public static AllurePreparedData of(File dataFile, TestSuiteResult testSuiteResult) {
        return new AllurePreparedData(dataFile, testSuiteResult);
    }

    private AllurePreparedData(File dataFile, TestSuiteResult suiteResult) {
        this.dataFile = dataFile;
        this.suiteResult = suiteResult;
    }
}
