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

package ru.bsc.test.at.executor.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import javax.jms.*;
import java.io.Closeable;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Map;

@Slf4j
abstract public class AbstractMqManager implements Closeable {

    abstract Connection getConnection();

    public void sendTextMessage(String queueName, String message, Map<String, Object> properties, String testIdHeaderName, String testId) throws Exception {
        Session session = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);

        Queue destination = session.createQueue(queueName);
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);

        TextMessage newMessage = session.createTextMessage(message);
        if (StringUtils.isNotEmpty(testIdHeaderName) && StringUtils.isNotEmpty(testId)) {
            newMessage.setStringProperty(testIdHeaderName, testId);
        }

        if (properties != null) {
            properties.forEach((name, value) -> {
                String stringValue = value instanceof String ? (String) value : null;
                try {
                    if (StringUtils.isNotEmpty(name) && value != null) {
                        if ("messageId".equals(name)) {
                            newMessage.setJMSMessageID(stringValue);
                        } else if ("correlationId".equals(name)) {
                            newMessage.setJMSCorrelationID(stringValue);
                        } else if ("replyTo".equals(name)) {
                            newMessage.setJMSReplyTo(session.createQueue(stringValue));
                        } else if ("timestamp".equals(name)) {
                            try {
                                DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss SSS");
                                newMessage.setJMSTimestamp(formatter.parse(stringValue).getTime());
                            } catch (ParseException e) {
                                log.error("{}", e);
                            }
                        } else {
                            newMessage.setObjectProperty(name, value);
                        }
                    }
                } catch (JMSException e) {
                    log.error("Set JMS object property error: {}", e);
                }
            });
        }

        producer.send(newMessage);

        producer.close();
        session.close();
    }

    public Message waitMessage(String queueName, Long timeoutMs, String testIdHeaderName, String testId) throws JMSException {
        Session session = getConnection().createSession(false, Session.AUTO_ACKNOWLEDGE);
        MessageConsumer consumer;
        if (StringUtils.isNotEmpty(testIdHeaderName) && StringUtils.isNotEmpty(testId)) {
            consumer = session.createConsumer(session.createQueue(queueName), testIdHeaderName + "='" + testId + "'");
        } else {
            consumer = session.createConsumer(session.createQueue(queueName));
        }
        return consumer.receive(timeoutMs);
    }
}
