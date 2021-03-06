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

package ru.bsc.test.autotester.report.impl.allure.attach.builder;

import ru.bsc.test.autotester.report.impl.allure.attach.extract.AttachExtractor;
import ru.yandex.qatools.allure.model.Attachment;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static ru.bsc.test.at.executor.utils.StreamUtils.nullSafeStream;

/**
 * Created by smakarov
 * 30.03.2018 12:17
 */
public abstract class AttachBuilder<T> {
    private final List<AttachExtractor<T>> extractors;

    public AttachBuilder(List<AttachExtractor<T>> extractors) {
        this.extractors = extractors;
    }

    public List<Attachment> build(File resultDirectory, T result) {
        return extractors.stream()
                .map(extractor -> extractor.extract(resultDirectory, result))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    public List<Attachment> buildAll(File resultDirectory, List<T> results) {
        return extractors.stream()
                .flatMap(extractor -> nullSafeStream(results).map(result -> extractor.extract(resultDirectory, result)))
                .filter(Objects::nonNull)
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }
}
