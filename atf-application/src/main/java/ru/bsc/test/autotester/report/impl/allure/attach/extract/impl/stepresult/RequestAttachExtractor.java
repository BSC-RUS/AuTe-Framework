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

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.executor.model.FieldType;
import ru.bsc.test.at.executor.model.RequestBodyType;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.autotester.report.impl.allure.attach.extract.impl.AbstractAttachExtractor;
import ru.yandex.qatools.allure.model.Attachment;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by smakarov
 * 23.03.2018 10:10
 */
@Component
public class RequestAttachExtractor extends AbstractAttachExtractor<StepResult> {

    private static final String FILE_NAME = "Request data";

    @Override
    public List<Attachment> extract(File resultDirectory, StepResult result) {
        if (StringUtils.isEmpty(result.getStep().getRelativeUrl())) {
            return null;
        }
        String requestData = getRequestData(result);
        String relativePath = writeDataToFile(resultDirectory, requestData, FILE_NAME);
        if (relativePath != null) {
            return Collections.singletonList(new Attachment()
                    .withTitle(FILE_NAME)
                    .withSource(relativePath)
                    .withType(TEXT_PLAIN));
        }
        return null;
    }

    private String getRequestData(StepResult result) {
        Step step = result.getStep();
        String data = "URL: " + step.getRelativeUrl();
        data += "\nMethod: " + step.getRequestMethod();
        data += "\nIgnore response: " + (step.getExpectedResponseIgnore() ? "yes" : "no");
        if (StringUtils.isNotEmpty(step.getTimeoutMs())) {
            data += "\nTimeout, ms: " + step.getTimeoutMs();
        }
        if (StringUtils.isNotEmpty(step.getNumberRepetitions())) {
            data += "\nNumber of repetitions: " + step.getNumberRepetitions();
        }
        if (step.getExpectedStatusCode() != null) {
            data += "\nExpected status: " + step.getExpectedStatusCode();
        }
        if (step.getRequestBodyType() == null || step.getRequestBodyType() == RequestBodyType.JSON) {
            data += "\nRequest body [json data]:\n";
            data += step.getRequest();
        } else {
            data += "\nRequest body " + (step.getMultipartFormData() ? "[multipart/form-data]" : "[form-data]") + "\n";
            data += step.getFormDataList().stream()
                    .map(formData ->
                            formData.getFieldName() +
                            (formData.getFieldType() == FieldType.TEXT ?
                             " = " + formData.getValue() :
                             "path: " + formData.getFilePath() + ", mime type: " + formData.getMimeType()))
                    .collect(Collectors.joining("\n"));
        }
        return data;
    }
}
