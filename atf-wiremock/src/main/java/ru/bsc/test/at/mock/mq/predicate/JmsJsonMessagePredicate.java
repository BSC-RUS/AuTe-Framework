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

import com.jayway.jsonpath.DocumentContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.models.MockMessage;

import java.util.List;

@Slf4j
public class JmsJsonMessagePredicate extends JmsMessagePredicate {

    private final DocumentContext messageBody;

    public JmsJsonMessagePredicate(String source, String testId, DocumentContext body) {
        super(source, testId);
        this.messageBody = body;
    }

    @Override
    public boolean test(MockMessage message) {
        return super.test(message) && testJsonPath(message);
    }

    private boolean testJsonPath(MockMessage message) {
        if (StringUtils.isEmpty(message.getXpath())) {
            return true;
        }

        Object result = messageBody.read(message.getXpath());
        if (result == null){
            return false;
        } else if (result instanceof List){
            List<?> resultList = (List<?>) result;
            return !resultList.isEmpty();
        }

        return true;
    }
}
