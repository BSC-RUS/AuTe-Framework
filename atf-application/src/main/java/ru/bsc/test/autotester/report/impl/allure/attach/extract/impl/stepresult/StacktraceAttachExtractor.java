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

package ru.bsc.test.autotester.report.impl.allure.attach.extract.impl.stepresult;

import org.springframework.stereotype.Component;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.autotester.report.impl.allure.attach.extract.impl.AbstractAttachExtractor;
import ru.yandex.qatools.allure.model.Attachment;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * Created by smakarov
 * 23.03.2018 11:03
 */
@Component
public class StacktraceAttachExtractor extends AbstractAttachExtractor<StepResult> {

    private static final String FILE_NAME = "Stacktrace";

    @Override
    public List<Attachment> extract(File resultDirectory, StepResult result) {
        if (result.getResult().isPositive()) {
            return null;
        }
        String relativePath = writeDataToFile(resultDirectory, result.getDetails(), FILE_NAME);
        if (relativePath != null) {
            return Collections.singletonList(new Attachment()
                    .withTitle(FILE_NAME)
                    .withSource(relativePath)
                    .withType(TEXT_PLAIN));
        }
        return null;
    }
}
