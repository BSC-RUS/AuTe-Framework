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
import org.w3c.dom.Document;
import ru.bsc.test.at.mock.mq.models.MockMessage;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

/**
 * Created by smakarov
 * 17.07.2019 11:04
 */
@Slf4j
public class JmsXmlMessagePredicate extends JmsMessagePredicate {
    private final Document messageBody;

    public JmsXmlMessagePredicate(String source, String testId, Document body) {
        super(source, testId);
        this.messageBody = body;
    }

    @Override
    public boolean test(MockMessage message) {
        return super.test(message) && testXpath(message);
    }

    private boolean testXpath(MockMessage message) {
        if (StringUtils.isEmpty(message.getXpath())) {
            return true;
        }
        try {
            return null != XPathFactory.newInstance().newXPath().evaluate(
                    message.getXpath(),
                    messageBody,
                    XPathConstants.NODE
            );
        } catch (XPathExpressionException e) {
            log.warn("XPath problem: {}", e.getMessage());
            return false;
        }
    }
}
