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

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.bsc.test.at.mock.mq.http.HttpClient;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.models.MockMessageResponse;
import ru.bsc.test.at.mock.mq.models.MockedRequest;
import ru.bsc.velocity.transformer.VelocityTransformer;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

// http://activemq.apache.org/hello-world.html

@SuppressWarnings("Duplicates")
public class ActiveMQWorker extends AbstractMqWorker {

    private static final Logger logger = LoggerFactory.getLogger(ActiveMQWorker.class);

    private final Buffer fifo;

    public ActiveMQWorker(String queueNameFrom, String queueNameTo, List<MockMessage> mockMappingList, Buffer fifo, String brokerUrl, String username, String password, String testIdHeaderName) {
        super(queueNameFrom, queueNameTo, mockMappingList, brokerUrl, username, password, testIdHeaderName);
        this.fifo = fifo;
    }

    @Override
    void runWorker() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(username, password, brokerUrl);

        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(session.createQueue(queueNameFrom));

            try {
                while (!Thread.currentThread().isInterrupted()) {

                    // Wait for a message
                    Message receivedMessage = consumer.receive();
                    if (!(receivedMessage instanceof ActiveMQTextMessage)) {
                        // Skip non text messages
                        continue;
                    }
                    ActiveMQTextMessage message = (ActiveMQTextMessage) receivedMessage;
                    String stringBody = message.getText();
                    logger.debug("Received: {}", stringBody);

                    MockedRequest mockedRequest = new MockedRequest();
                    //noinspection unchecked
                    fifo.add(mockedRequest);
                    mockedRequest.setSourceQueue(queueNameFrom);

                    String testId = message.getStringProperty("testIdHeaderName");
                    mockedRequest.setTestId(testId);

                    MockMessage mockMessage = findMockMessage(testId, stringBody);
                    if (mockMessage != null) {
                        mockedRequest.setMappingGuid(mockMessage.getGuid());

                        // Выполнение инструкции из моков
                        for (MockMessageResponse mockResponse : mockMessage.getResponses()) {
                            byte[] response;

                        if (StringUtils.isNotEmpty(mockResponse.getResponseBody())) {
                            response = new VelocityTransformer().transform(stringBody, null, mockResponse.getResponseBody()).getBytes();
                        } else if (StringUtils.isNotEmpty(mockMessage.getHttpUrl())) {
                            try (HttpClient httpClient = new HttpClient()) {
                            response = httpClient.sendPost(mockMessage.getHttpUrl(), new String(message.getContent().getData(), StandardCharsets.UTF_8), testIdHeaderName, testId).getBytes();}
                            mockedRequest.setHttpRequestUrl(mockMessage.getHttpUrl());
                        } else {
                            response = stringBody.getBytes();
                        }

                            mockedRequest.setDestinationQueue(mockResponse.getDestinationQueueName());

                            if (isNotEmpty(mockResponse.getDestinationQueueName())) {

                                mockedRequest.setResponseBody(new String(response, StandardCharsets.UTF_8));

                                Queue destination = session.createQueue(mockResponse.getDestinationQueueName());
                                MessageProducer producer = session.createProducer(destination);
                                producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
                                TextMessage newMessage = session.createTextMessage(new String(response, StandardCharsets.UTF_8));
                                newMessage.getPropertyNames();
                                copyMessageProperties(message, newMessage, testId, destination);
                                // Переслать сообщение в очередь-назначение
                                producer.send(newMessage);

                                producer.close();
                            }
                        }
                    } else {
                        // Переслать сообщение в очередь "по-умолчанию".
                        mockedRequest.setDestinationQueue(queueNameTo);
                        if (isNotEmpty(queueNameTo)) {
                            mockedRequest.setResponseBody(stringBody);

                            Queue destination = session.createQueue(queueNameTo);
                            MessageProducer producer = session.createProducer(destination);
                            ActiveMQMessage newMessage = (ActiveMQMessage)session.createMessage();
                            // Message newMessage = message.copy();
                            newMessage.getPropertyNames();
                            newMessage.setStringProperty(testIdHeaderName, testId);
                            newMessage.setContent(message.getContent());

                            newMessage.setJMSDestination(destination);
                            // Переслать сообщение в очередь-назначение
                            producer.send(newMessage);
                            producer.close();
                            logger.info(" [x] Send >>> {} '{}'", queueNameTo, new String(message.getContent().getData(), "UTF-8"));
                        } else {
                            logger.info(" [x] Send >>> ***black hole***");
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("Caught: {}", e);
            }

            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            logger.error("Caught: {}", e);
        }
    }

    @Override
    public void stop() {
        // Do nothing
    }
}
