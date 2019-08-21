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

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.ibm.msg.client.wmq.WMQConstants;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.JmsMessageHeadersExtractor;
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
    public void run() {
        MQQueueConnectionFactory connectionFactory;
        try {
            connectionFactory = new MQQueueConnectionFactory();
            connectionFactory.setHostName(getBrokerUrl());
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
            Connection connection = connectionFactory.createConnection(getUsername(), getPassword());
            connection.start();
            Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(session.createQueue(getQueueNameFrom()));
            JmsMessageHeadersExtractor extractor = new JmsMessageHeadersExtractor();
            VelocityTransformer transformer = new VelocityTransformer();

            try {
                while (!Thread.currentThread().isInterrupted()) {
                    log.info("Wait messages from {}", getQueueNameFrom());
                    Message receivedMessage = consumer.receive();
                    String jmsMessageId = receivedMessage.getJMSMessageID();
                    String jmsCorrelationId = receivedMessage.getJMSCorrelationID();
                    String jmsReplyTo = receivedMessage.getJMSReplyTo() == null ? null : receivedMessage.getJMSReplyTo().toString();

                    log.info("Received message: JMSMessageID={}, JMSReplyTo={}, JmsCorrelationID={}", jmsMessageId, jmsReplyTo, jmsCorrelationId);
                    log.info("Received message: {}", receivedMessage);

                    MockedRequest mockedRequest = new MockedRequest();
                    //noinspection unchecked
                    fifo.add(mockedRequest);
                    mockedRequest.setSourceQueue(getQueueNameFrom());

                    String stringBody;
                    if (receivedMessage instanceof TextMessage) {
                        TextMessage message = (TextMessage) receivedMessage;
                        stringBody = message.getText();
                    } else if (receivedMessage instanceof BytesMessage) {
                        BytesMessage byteMessage = (BytesMessage) receivedMessage;
                        byte[] byteData = new byte[(int) byteMessage.getBodyLength()];
                        byteMessage.readBytes(byteData);
                        stringBody = new String(byteData).replaceAll("<\\?xml(.+?)\\?>", "").trim();
                    } else {
                        mockedRequest.setRequestBody("<not text or byte message>");
                        continue;
                    }
                    mockedRequest.setRequestBody(stringBody);
                    log.info(" [x] Received <<< {} {}", getQueueNameFrom(), stringBody);

                    String testId = receivedMessage.getStringProperty(testIdHeaderName);
                    if (StringUtils.isEmpty(testId)) {
                        testIdHeaderName = convertTestIdHeaderName();
                        testId = receivedMessage.getStringProperty(testIdHeaderName);
                    }
                    mockedRequest.setTestId(testId);

                    MockMessage mockMessage = findMockMessage(testId, stringBody);
                    log.debug("found mock message: {}", mockMessage);
                    if (mockMessage != null) {
                        mockedRequest.setMappingGuid(mockMessage.getGuid());

                        // Выполнение инструкции из моков
                        for (MockMessageResponse mockResponse : mockMessage.getResponses()) {
                            byte[] response;

                            if (StringUtils.isNotEmpty(mockResponse.getResponseBody())) {
                                response = transformer.transform(stringBody, extractor.createContext(message), mockResponse.getResponseBody()).getBytes();
                            } else if (StringUtils.isNotEmpty(mockMessage.getHttpUrl())) {
                                try (HttpClient httpClient = new HttpClient()) {
                                    response = httpClient.sendPost(mockMessage.getHttpUrl(), stringBody, getTestIdHeaderName(), testId).getBytes();
                                }
                                mockedRequest.setHttpRequestUrl(mockMessage.getHttpUrl());
                            } else {
                                response = stringBody.getBytes();
                            }

                            String destinationQueue = isNotEmpty(mockResponse.getDestinationQueueName())
                                                      ? mockResponse.getDestinationQueueName()
                                                      : jmsReplyTo;
                            sendMessage(session, receivedMessage, mockedRequest, destinationQueue, new String(response, StandardCharsets.UTF_8), testId, DeliveryMode.NON_PERSISTENT);
                        }
                    } else {
                        // Переслать сообщение в очередь "по-умолчанию".
                        sendMessage(session, receivedMessage, mockedRequest, queueNameTo, stringBody, testId, null);
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
    private void sendMessage(Session session, Message receivedMessage, MockedRequest mockedRequest, String destinationQueue, String messageBody, String testId, Integer deliveryMode) throws JMSException {
        if (isNotEmpty(destinationQueue)) {
            mockedRequest.setDestinationQueue(destinationQueue);
            mockedRequest.setResponseBody(messageBody);

            Queue destination = session.createQueue(destinationQueue);
            MessageProducer producer = session.createProducer(destination);
            extractor.setHeadersFromContext(newMessage, transformer.getVelocityContext());

            if (deliveryMode != null) {
                producer.setDeliveryMode(deliveryMode);
            }

            Message newMessage = session.createTextMessage(messageBody);
            copyMessageProperties(receivedMessage, newMessage, testId, destination);

            // Переслать сообщение в очередь-назначение
            producer.send(newMessage);
            producer.close();
            log.info(" [x] Send >>> {} '{}'", destinationQueue, messageBody, StandardCharsets.UTF_8);
        } else {
            log.info(" [x] Send >>> ***black hole***");
        }
    }

    private String convertTestIdHeaderName(){
        return testIdHeaderName.contains("-")
               ? testIdHeaderName.toLowerCase().replace("-", "_HYPHEN_")
               : testIdHeaderName.toLowerCase().replace("_HYPHEN_", "-") ;
    }
}
