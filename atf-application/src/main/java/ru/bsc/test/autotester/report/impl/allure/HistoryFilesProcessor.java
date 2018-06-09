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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.history.HistoryData;
import io.qameta.allure.history.HistoryItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bsc.test.autotester.properties.EnvironmentProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static ru.bsc.test.autotester.report.impl.allure.plugin.HistoryRestorePlugin.*;

/**
 * Created by smakarov
 * 10.04.2018 12:57
 */
@Component
@Slf4j
public class HistoryFilesProcessor {
    private static final Integer DEFAULT_HISTORY_LIMIT = 10;
    //@formatter:off
    private static final TypeReference<Map<String, HistoryData>> HISTORY_TYPE =
            new TypeReference<Map<String, HistoryData>>() {};
    //@formatter:on

    private final ObjectMapper mapper;
    private final int historyLimit;

    public HistoryFilesProcessor(ObjectMapper mapper, EnvironmentProperties properties) {
        this.mapper = mapper;
        Integer historyLimit = properties.getHistoryLimit();
        this.historyLimit = historyLimit != null ? historyLimit : DEFAULT_HISTORY_LIMIT;
    }

    public void process(String projectCode, Path output) {
        copyHistoryFile(projectCode, output, HISTORY_JSON, this::processHistory);
        copyHistoryFile(projectCode, output, HISTORY_TREND_JSON);
        copyHistoryFile(projectCode, output, DURATION_TREND_JSON);
    }

    private void copyHistoryFile(String projectCode, Path output, String fileName) {
        copyHistoryFile(projectCode, output, fileName, null);
    }

    private void copyHistoryFile(String projectCode, Path output, String fileName, Consumer<Path> fileProcessor) {
        try {
            Path source = output.resolve(HISTORY_DIRECTORY).resolve(fileName);
            if (Files.exists(source)) {
                Path targetPath = Paths.get(HISTORY_DIRECTORY).resolve(projectCode);
                if (!Files.exists(targetPath)) {
                    Files.createDirectories(targetPath);
                }
                if (fileProcessor != null) {
                    fileProcessor.accept(source);
                }
                Files.copy(source, targetPath.resolve(fileName), StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            log.error("Error while copy report file {}", fileName, e);
        }
    }

    private void processHistory(Path path) {
        try {
            final Map<String, HistoryData> history = mapper.readValue(path.toFile(), HISTORY_TYPE);
            history.values().forEach(historyData -> {
                List<HistoryItem> historyItems = historyData.getItems();
                if (historyItems.size() > historyLimit) {
                    historyItems = historyItems.subList(0, historyLimit);
                    historyData.setItems(historyItems);
                }
            });
            mapper.writeValue(path.toFile(), history);
        } catch (IOException e) {
            log.error("Error while process history.json file", e);
        }
    }
}
