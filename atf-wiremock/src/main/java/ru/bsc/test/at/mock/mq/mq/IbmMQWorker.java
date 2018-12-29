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

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.http.HttpClient;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.models.MockMessageResponse;
import ru.bsc.test.at.mock.mq.models.MockedRequest;
import ru.bsc.velocity.transformer.VelocityTransformer;

import javax.jms.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
@SuppressWarnings("Duplicates")
public class IbmMQWorker extends AbstractMqWorker {

    private final Buffer fifo;
    private Integer port;
    private String channel;

    public IbmMQWorker(String queueNameFrom, String queueNameTo, List<MockMessage> mockMappingList, Buffer fifo, String brokerUrl, String username, String password, Integer port, String testIdHeaderName, String channel) {
        super(queueNameFrom, queueNameTo, mockMappingList, brokerUrl, username, password, testIdHeaderName);
        this.fifo = fifo;
        this.port = port;
        this.channel = channel;
    }

    @Override
    void runWorker() {
        MQQueueConnectionFactory connectionFactory;
        try {
            connectionFactory = new MQQueueConnectionFactory();
            connectionFactory.setHostName(brokerUrl);
            connectionFactory.setPort(port);
            connectionFactory.setTransportType(WMQConstants.WMQ_CM_CLIENT);
            if (StringUtils.isNotEmpty(channel)) {
                connectionFactory.setChannel(channel);
            }
        } catch (JMSException e) {
            log.error("exception while create connection factory:", e);
            return;
        }

        try {
            Connection connection = connectionFactory.createConnection(username, password);
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

            MessageConsumer consumer = session.createConsumer(session.createQueue(queueNameFrom));

            try {
                while (!Thread.currentThread().isInterrupted()) {

                    // Wait for a message
                    log.info("Wait messages from {}", queueNameFrom);
                    Message receivedMessage = consumer.receive();
                    log.info("Received message, JMSMessageID: {}", receivedMessage.getJMSMessageID());

                    MockedRequest mockedRequest = new MockedRequest();
                    //noinspection unchecked
                    fifo.add(mockedRequest);
                    mockedRequest.setSourceQueue(queueNameFrom);

                    if (!(receivedMessage instanceof TextMessage)) {
                        mockedRequest.setRequestBody("<not text message>");
                        continue;
                    }
                    TextMessage message = (TextMessage) receivedMessage;
                    String stringBody = message.getText();
                    mockedRequest.setRequestBody(stringBody);
                    log.info(" [x] Received <<< {} {}", queueNameFrom, stringBody);

                    String testId = message.getStringProperty(testIdHeaderName);
                    mockedRequest.setTestId(testId);

                    MockMessage mockMessage = findMockMessage(testId, stringBody);
                    log.debug("found mock message: {}", mockMessage);
                    if (mockMessage != null) {
                        mockedRequest.setMappingGuid(mockMessage.getGuid());

                        // Выполнение инструкции из моков
                        for (MockMessageResponse mockResponse : mockMessage.getResponses()) {
                            byte[] response;

                            if (StringUtils.isNotEmpty(mockResponse.getResponseBody())) {
                                response = new VelocityTransformer().transform(stringBody, null, mockResponse.getResponseBody()).getBytes();
                            } else if (StringUtils.isNotEmpty(mockMessage.getHttpUrl())) {
                                try (HttpClient httpClient = new HttpClient()) {
                                    response = httpClient.sendPost(mockMessage.getHttpUrl(), message.getText(), testIdHeaderName, testId).getBytes();
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
                                copyMessageProperties(message, newMessage, testId, destination);

                                // Переслать сообщение в очередь-назначение
                                producer.send(newMessage);

                                producer.close();
                                log.info(" [x] Send >>> {} '{}'", mockResponse.getDestinationQueueName(), message.getText(), StandardCharsets.UTF_8);
                            }
                        }
                    } else {
                        // Переслать сообщение в очередь "по-умолчанию".
                        mockedRequest.setDestinationQueue(queueNameTo);
                        if (isNotEmpty(queueNameTo)) {
                            mockedRequest.setResponseBody(stringBody);

                            Queue destination = session.createQueue(queueNameTo);
                            MessageProducer producer = session.createProducer(destination);
                            TextMessage newMessage = session.createTextMessage(message.getText());

                            copyMessageProperties(message, newMessage, testId, destination);

                            // Переслать сообщение в очередь-назначение
                            producer.send(newMessage);
                            producer.close();
                            log.info(" [x] Send >>> {} '{}'", queueNameTo, message.getText(), "UTF-8");
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

    @Override
    public void stop() {
        // Do nothing
    }
}
