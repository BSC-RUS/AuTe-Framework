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

import com.rabbitmq.client.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.Buffer;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.JmsMessageHeadersExtractor;
import ru.bsc.test.at.mock.mq.components.MqProperties;
import ru.bsc.test.at.mock.mq.http.HttpClient;
import ru.bsc.test.at.mock.mq.models.*;
import ru.bsc.velocity.transformer.VelocityTransformer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Slf4j
public class RabbitMQWorker extends AbstractMqWorker {
    private Channel channelFrom;
    private Channel channelTo;
    private com.rabbitmq.client.Connection connection;
    private VelocityTransformer velocityTransformer;

    public RabbitMQWorker(String sourceQueueName, MqProperties properties, List<MockMessage> mappings, Buffer fifo, String testIdHeaderName) {
        super(sourceQueueName, properties, mappings, fifo, testIdHeaderName);
    }

    @Override
    public void run() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(getProperties().getHost());
            connectionFactory.setPort(getProperties().getPort());
            connectionFactory.setUsername(getProperties().getUsername());
            connectionFactory.setPassword(getProperties().getPassword());

            connection = connectionFactory.newConnection();
            channelFrom = connection.createChannel();
            channelTo = connection.createChannel();
            velocityTransformer = new VelocityTransformer();

            try {
                // Wait for a message
                waitMessage();
            } catch (Exception e) {
                log.error("Caught:", e);
            }
        } catch (Exception e) {
            log.error("Caught:", e);
        }
    }

    @Override
    public void stop() throws IOException, TimeoutException {
        if (channelFrom.isOpen()) {
            channelFrom.close();
        }
        if (channelTo.isOpen()) {
            channelTo.close();
        }
        connection.close();
    }

    private void waitMessage() throws IOException {
        channelFrom.basicConsume(getQueueNameFrom(), true, new DefaultConsumer(channelFrom) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                String stringBody = new String(body, StandardCharsets.UTF_8);
                log.info(" [x] Received '{}'", stringBody);

                MockedRequest mockedRequest = new MockedRequest();
                //noinspection unchecked
                getFifo().add(mockedRequest);
                mockedRequest.setRequestBody(stringBody);
                mockedRequest.setSourceQueue(getQueueNameFrom());

                // Найти соответствие по названию очереди и testIdProperty
                String testId = properties.getHeaders().get(getTestIdHeaderName()) == null ? null : properties.getHeaders().get(getTestIdHeaderName()).toString();
                mockedRequest.setTestId(testId);

                try {
                    MockMessage mockMessage = findMockMessage(testId, stringBody);
                    log.debug("found mock message: {}", mockMessage);
                    if (mockMessage != null) {
                        sendMockMessage(mockedRequest, mockMessage, properties, body, stringBody, testId);
                    } else {
                        sendMessageToDefaultQueue(mockedRequest, properties, body, stringBody);
                    }
                } catch (Exception e) {
                    log.error("Error while sending mock message: properties - {}, body - {}", properties, body, e);
                }
            }
        });
    }

    private void sendMockMessage(MockedRequest mockedRequest, MockMessage mockMessage, AMQP.BasicProperties properties, byte[] body, String stringBody, String testId) throws IOException {

        mockedRequest.setMappingGuid(mockMessage.getGuid());
        for (MockMessageResponse mockResponse : mockMessage.getResponses()) {
            byte[] response;

            if (StringUtils.isNotEmpty(mockResponse.getResponseBody())) {
                JmsMessageHeadersExtractor extractor = new JmsMessageHeadersExtractor();
                response = velocityTransformer
                    .transform(mockMessage.getGuid(), stringBody, extractor.createContext(properties.getHeaders(), body), mockResponse.getResponseBody())
                    .getBytes();
            } else if (StringUtils.isNotEmpty(mockMessage.getHttpUrl())) {
                try (HttpClient httpClient = new HttpClient()) {
                    response = httpClient.sendPost(mockMessage.getHttpUrl(), new String(body, StandardCharsets.UTF_8), getTestIdHeaderName(), testId).getBytes();
                }
                mockedRequest.setHttpRequestUrl(mockMessage.getHttpUrl());
            } else {
                response = body;
            }

            mockedRequest.setDestinationQueue(mockResponse.getDestinationQueueName());

            //noinspection ConstantConditions
            if (isNotEmpty(mockResponse.getDestinationQueueName()) && response != null) {
                mockedRequest.setResponseBody(new String(response, StandardCharsets.UTF_8));

                try (Channel channel = connection.createChannel()) {
                    channel.basicPublish("", mockResponse.getDestinationQueueName(), properties, response);
                    log.info(" [x] Send >>> {} '{}'", mockResponse.getDestinationQueueName(), new String(response, StandardCharsets.UTF_8));
                } catch (TimeoutException e) {
                    log.error("Caught:", e);
                }
            }
        }
    }

    private void sendMessageToDefaultQueue(MockedRequest mockedRequest, AMQP.BasicProperties properties, byte[] body, String stringBody) throws IOException {
        // Переслать сообщение в очередь "по-умолчанию".
        String defaultQueue = getProperties().getDefaultDestinationQueueName();
        mockedRequest.setDestinationQueue(defaultQueue);
        if (isNotEmpty(defaultQueue)) {
            mockedRequest.setResponseBody(stringBody);
            channelTo.basicPublish("", defaultQueue, properties, body);
            log.info(" [x] Send >>> {} '{}'", defaultQueue, new String(body, StandardCharsets.UTF_8));
        } else {
            log.info(" [x] Send >>> ***black hole***");
        }
    }
}
