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

package ru.bsc.test.at.mock.mq.predicate;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.models.MockMessage;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by smakarov
 * 17.07.2019 11:04
 */
@Slf4j
public class JmsMessagePredicate implements Predicate<MockMessage> {
    private final String source;
    private final String testId;

    public JmsMessagePredicate(String source, String testId) {
        this.source = source;
        this.testId = testId;
    }

    @Override
    public boolean test(MockMessage message) {
        return testSourceName(message) && testTestId(message);
    }

    private boolean testSourceName(MockMessage message) {
        return Objects.equals(source, message.getSourceQueueName());
    }

    private boolean testTestId(MockMessage message) {
        return Objects.equals(testId, message.getTestId()) || StringUtils.isEmpty(message.getTestId());
    }

}
