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

package ru.bsc.test.at.mock.mq.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import ru.bsc.test.at.mock.mq.models.MockMessage;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

abstract public class AbstractMqWorker implements Runnable, ExceptionListener {

    private static final Logger logger = LoggerFactory.getLogger(AbstractMqWorker.class);

    final String queueNameFrom;
    final String queueNameTo;
    private final List<MockMessage> mockMappingList;

    final String brokerUrl;
    final String username;
    final String password;

    String testIdHeaderName;

    AbstractMqWorker(
            String queueNameFrom,
            String queueNameTo,
            List<MockMessage> mockMappingList,
            String brokerUrl,
            String username,
            String password,
            String testIdHeaderName
    ) {
        this.queueNameFrom = queueNameFrom;
        this.queueNameTo = queueNameTo;
        this.mockMappingList = mockMappingList;
        this.brokerUrl = brokerUrl;
        this.username = username;
        this.password = password;
        this.testIdHeaderName = testIdHeaderName;
    }

    public void run() {
        runWorker();
    }

    abstract void runWorker();

    public abstract void stop() throws IOException, TimeoutException;

    public synchronized void onException(JMSException ex) {
        logger.error("{}", ex);
    }

    private Document parseXmlDocument(String stringBody) {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            return db.parse(new InputSource(new StringReader(stringBody)));
        } catch (IOException | ParserConfigurationException | SAXException e) {
            logger.info("Cannot parse XML document: {}", e.getMessage());
            return null;
        }
    }

    MockMessage findMockMessage(String testId, String stringBody) {
        synchronized (mockMappingList) {
            final Document document = parseXmlDocument(stringBody);

            Predicate<MockMessage> documentXpathFilterPredicate = message -> {
                if (message.getXpath() == null) {
                    return true;
                }
                try {
                    Object node = XPathFactory.newInstance().newXPath().evaluate(
                            message.getXpath(),
                            document,
                            XPathConstants.NODE
                    );
                    return node != null;
                } catch (XPathExpressionException e) {
                    return false;
                }
            };

            return mockMappingList
                    .stream()
                    .filter(message -> Objects.equals(queueNameFrom, message.getSourceQueueName()))
                    .filter(message -> Objects.equals(testId, message.getTestId()))
                    .filter(documentXpathFilterPredicate)
                    .findAny()
                    .orElse(mockMappingList
                            .stream()
                            .filter(message -> Objects.equals(queueNameFrom, message.getSourceQueueName()))
                            .filter(message -> message.getTestId() == null)
                            .filter(documentXpathFilterPredicate)
                            .findAny()
                            .orElse(null)
                    );
        }
    }

    void copyMessageProperties(
            TextMessage message,
            TextMessage newMessage,
            String testId,
            Queue destination
    ) throws JMSException {
        newMessage.setJMSCorrelationID(message.getJMSMessageID());

        newMessage.setStringProperty(testIdHeaderName, testId);
        newMessage.setJMSDestination(destination);
    }
}
