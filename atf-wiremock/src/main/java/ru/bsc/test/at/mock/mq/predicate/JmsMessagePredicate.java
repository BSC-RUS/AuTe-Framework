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
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.bsc.test.at.mock.mq.models.MockMessage;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.Objects;
import java.util.function.Predicate;

/**
 * Created by smakarov
 * 17.07.2019 11:04
 */
@Slf4j
public class JmsMessagePredicate implements Predicate<MockMessage> {
    private final String source;
    private final Document messageBody;
    private final String testId;

    public JmsMessagePredicate(String source, String body, String testId) {
        this.source = source;
        this.messageBody = parseXml(body);
        this.testId = testId;
    }

    @Override
    public boolean test(MockMessage message) {
        return testSourceName(message) && testTestId(message) && testXpath(message);
    }

    private boolean testSourceName(MockMessage message) {
        return Objects.equals(source, message.getSourceQueueName());
    }

    private boolean testTestId(MockMessage message) {
        return Objects.equals(testId, message.getTestId());
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

    private Document parseXml(String stringBody) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(stringBody)));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            log.info("Cannot parse XML document: {}", e.getMessage());
            return null;
        }
    }
}
