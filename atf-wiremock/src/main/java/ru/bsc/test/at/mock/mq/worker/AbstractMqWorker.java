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

package ru.bsc.test.at.mock.mq.worker;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.components.MqProperties;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.predicate.JmsMessagePredicate;
import ru.bsc.test.at.mock.mq.predicate.JmsMessagePredicateFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Queue;

@Slf4j
@Getter
@RequiredArgsConstructor
public abstract class AbstractMqWorker implements Runnable, ExceptionListener {
    private final String queueNameFrom;
    private final MqProperties properties;
    private final List<MockMessage> mockMappingList;
    private final Buffer fifo;
    private final String testIdHeaderName;

    public void stop() throws IOException, TimeoutException {

    }

    public synchronized void onException(JMSException ex) {
        log.error("", ex);
    }

    MockMessage findMockMessage(String testId, String stringBody) {
        synchronized (mockMappingList) {
            final JmsMessagePredicate jmsMessageFilter = JmsMessagePredicateFactory.getInstance().newJmsMessagePredicate(queueNameFrom, testId, stringBody);
            return mockMappingList
                    .stream()
                    .sorted()
                    .filter(jmsMessageFilter)
                    .findAny()
                    .orElse(null);
        }
    }

    void copyMessageProperties(Message message, Message newMessage, String testId, Queue destination, String testIdHeaderName) throws JMSException {
        String jmsCorrelationId = message.getJMSCorrelationID();
        newMessage.setJMSCorrelationID(StringUtils.isNotEmpty(jmsCorrelationId) ? jmsCorrelationId : message.getJMSMessageID());

        newMessage.setStringProperty(testIdHeaderName, testId);
        newMessage.setJMSDestination(destination);
    }
}
