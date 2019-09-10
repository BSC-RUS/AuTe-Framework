/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

package ru.bsc.test.at.mock.wiremock.repository.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.mock.mq.models.JmsMappings;
import ru.bsc.test.at.mock.wiremock.repository.JmsMappingsRepository;
import ru.bsc.test.at.util.YamlUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by smakarov
 * 03.07.2019 11:06
 */
@Slf4j
@Component
public class JmsMappingsRepositoryImpl implements JmsMappingsRepository {
    private static final Path JMS_MAPPINGS_PATH = Paths.get("jms-mappings", "mappings.yaml");

    @Override
    public boolean save(JmsMappings mappings) {
        return YamlUtils.saveToFile(mappings, JMS_MAPPINGS_PATH);
    }

    @Override
    public JmsMappings load() {
        try {
            return YamlUtils.loadAs(JMS_MAPPINGS_PATH, JmsMappings.class).orElseGet(JmsMappings::new);
        } catch (IOException e) {
            log.error("Error while loading JMS mappings", e);
            return new JmsMappings();
        }
    }
}
