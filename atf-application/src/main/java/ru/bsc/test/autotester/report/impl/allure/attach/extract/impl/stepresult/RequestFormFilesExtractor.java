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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.executor.model.FieldType;
import ru.bsc.test.at.executor.model.FormData;
import ru.bsc.test.at.executor.model.StepResult;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.report.impl.allure.attach.extract.impl.AbstractAttachExtractor;
import ru.yandex.qatools.allure.model.Attachment;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Created by smakarov
 * 03.04.2018 13:49
 */
@Component
@Slf4j
public class RequestFormFilesExtractor extends AbstractAttachExtractor<StepResult> {

    private final String projectRepositoryPath;

    @Autowired
    public RequestFormFilesExtractor(EnvironmentProperties environmentProperties) {
        this.projectRepositoryPath = environmentProperties.getProjectsDirectoryPath();
    }

    @Override
    public List<Attachment> extract(File resultDirectory, StepResult result) {
        if (CollectionUtils.isEmpty(result.getStep().getFormDataList())) {
            return null;
        }
        return result.getStep().getFormDataList().stream()
                .filter(data -> data.getFieldType() == FieldType.FILE)
                .filter(data -> StringUtils.isNotEmpty(data.getFilePath()))
                .map(data -> mapDataToAttachment(resultDirectory, result, data))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Attachment mapDataToAttachment(File resultDirectory, StepResult stepResult, FormData formData) {
        String extension = FilenameUtils.getExtension(formData.getFilePath());
        String relativePath = getAttachRelativePath(formData.getFieldName(), extension);
        Path sourcePath = Paths.get(projectRepositoryPath, stepResult.getProjectCode(), formData.getFilePath());
        Path attachPath = resultDirectory.toPath().resolve(relativePath);
        String contentType;
        try {
            contentType = Files.probeContentType(sourcePath);
        } catch (IOException ignored) {
            contentType = TEXT_PLAIN;
        }
        try {
            Files.copy(sourcePath, attachPath);
        } catch (IOException e) {
            log.warn("Exception while copying form data file: {}", formData.getFilePath(), e);
        }
        return new Attachment().withTitle(formData.getFieldName()).withSource(relativePath).withType(contentType);
    }
}
