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

import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.JmsMessageHeadersExtractor;
import ru.bsc.test.at.mock.mq.components.MqProperties;
import ru.bsc.test.at.mock.mq.http.HttpClient;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.models.MockMessageResponse;
import ru.bsc.test.at.mock.mq.models.MockedRequest;
import ru.bsc.velocity.transformer.VelocityTransformer;

import java.nio.charset.StandardCharsets;
import java.util.List;
import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

// http://activemq.apache.org/hello-world.html

@Slf4j
@SuppressWarnings("Duplicates")
public class ActiveMQWorker extends AbstractMqWorker {
    public ActiveMQWorker(String sourceQueueName, MqProperties properties, List<MockMessage> mappings, Buffer fifo, String testIdHeaderName) {
        super(sourceQueueName, properties, mappings, fifo, testIdHeaderName);
    }

    @Override
    public void run() {
        ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(getProperties().getUsername(), getProperties().getPassword(), getBrokerUrl());

        try {
            Connection connection = connectionFactory.createConnection();
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(session.createQueue(getQueueNameFrom()));
            JmsMessageHeadersExtractor extractor = new JmsMessageHeadersExtractor();
            VelocityTransformer transformer = new VelocityTransformer();

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    Message receivedMessage = consumer.receive();
                    if (!(receivedMessage instanceof ActiveMQTextMessage)) {
                        continue;
                    }
                    ActiveMQTextMessage message = (ActiveMQTextMessage) receivedMessage;
                    String stringBody = message.getText();
                    log.debug("Received: {}", stringBody);

                    MockedRequest mockedRequest = new MockedRequest();
                    //noinspection unchecked
                    getFifo().add(mockedRequest);
                    mockedRequest.setSourceQueue(getQueueNameFrom());
                    mockedRequest.setRequestBody(stringBody);

                    String testId = message.getStringProperty(getTestIdHeaderName());
                    mockedRequest.setTestId(testId);

                    MockMessage mockMessage = findMockMessage(testId, stringBody);
                    log.debug("found mock message: {}", mockMessage);
                    if (mockMessage != null) {
                        mockedRequest.setMappingGuid(mockMessage.getGuid());

                        // Выполнение инструкции из моков
                        for (MockMessageResponse mockResponse : mockMessage.getResponses()) {
                            byte[] response;

                            if (StringUtils.isNotEmpty(mockResponse.getResponseBody())) {
                                response = transformer.transform(mockMessage.getGuid(), stringBody, extractor.createContext(message), mockResponse.getResponseBody()).getBytes();
                            } else if (StringUtils.isNotEmpty(mockMessage.getHttpUrl())) {
                                try (HttpClient httpClient = new HttpClient()) {
                                    response = httpClient.sendPost(mockMessage.getHttpUrl(), new String(message.getContent().getData(), StandardCharsets.UTF_8), getTestIdHeaderName(), testId).getBytes();
                                }
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
                                extractor.setHeadersFromContext(newMessage, transformer.getVelocityContext());
                                copyMessageProperties(message, newMessage, testId, destination, getTestIdHeaderName());
                                // Переслать сообщение в очередь-назначение
                                producer.send(newMessage);

                                producer.close();
                            }
                        }
                    } else {
                        // Переслать сообщение в очередь "по-умолчанию".
                        String defaultQueue = getProperties().getDefaultDestinationQueueName();
                        mockedRequest.setDestinationQueue(defaultQueue);
                        if (isNotEmpty(defaultQueue)) {
                            mockedRequest.setResponseBody(stringBody);

                            Queue destination = session.createQueue(defaultQueue);
                            MessageProducer producer = session.createProducer(destination);
                            ActiveMQMessage newMessage = (ActiveMQMessage) session.createMessage();
                            newMessage.setStringProperty(getTestIdHeaderName(), testId);
                            newMessage.setContent(message.getContent());

                            newMessage.setJMSDestination(destination);
                            // Переслать сообщение в очередь-назначение
                            producer.send(newMessage);
                            producer.close();
                            log.info(" [x] Send >>> {} '{}'", defaultQueue, new String(message.getContent().getData(), StandardCharsets.UTF_8));
                        } else {
                            log.info(" [x] Send >>> ***black hole***");
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Caught:", e);
            }

            consumer.close();
            session.close();
            connection.close();
        } catch (Exception e) {
            log.error("Caught:", e);
        }
    }

    private String getBrokerUrl() {
        return String.format("tcp://%s:%d", getProperties().getHost(), getProperties().getPort());
    }
}
