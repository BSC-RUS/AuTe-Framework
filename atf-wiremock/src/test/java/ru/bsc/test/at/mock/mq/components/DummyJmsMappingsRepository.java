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

package ru.bsc.test.at.mock.mq.components;

import ru.bsc.test.at.mock.mq.models.JmsMappings;
import ru.bsc.test.at.mock.wiremock.repository.JmsMappingsRepository;

/**
 * Created by smakarov
 * 03.07.2019 13:06
 */
public class DummyJmsMappingsRepository implements JmsMappingsRepository {
    @Override
    public boolean save(JmsMappings mappings) {
        return true;
    }

    @Override
    public JmsMappings load() {
        return new JmsMappings();
    }
}