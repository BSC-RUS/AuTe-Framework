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

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.jms.client.message.RMQTextMessage;
import lombok.extern.slf4j.Slf4j;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

// Это реализация для работы с RabbitMQ.
// Сообщения не отправляются в очередь через стандартные интерфейсы JMS, если у очереди указан параметр x-dead-letter-exchange

@Slf4j
class RabbitMqManager extends AbstractMqManager {

    private final com.rabbitmq.client.Connection senderConnection;

    RabbitMqManager(String host, int port, String username, String password) throws JMSException {

        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(host);
            connectionFactory.setPort(port);
            connectionFactory.setUsername(username);
            connectionFactory.setPassword(password);
            senderConnection = connectionFactory.newConnection();
        } catch (Exception e) {
            log.error("{}", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendTextMessage(String queueName, String message, Map<String, Object> properties, String testIdHeaderName, String testId) throws Exception {
        Channel channel = senderConnection.createChannel();

        AMQP.BasicProperties.Builder propertiesBuilder = new AMQP.BasicProperties().builder();

        Map<String, Object> headers = new HashMap<>();

        if (properties != null) {
            properties.forEach((name, value) -> {
                String stringValue = value instanceof String ? (String) value : null;
                if ("messageId".equals(name)) {
                    propertiesBuilder.messageId(stringValue);
                } else if ("contentType".equals(name)) {
                    propertiesBuilder.contentType(stringValue);
                } else if ("contentEncoding".equals(name)) {
                    propertiesBuilder.contentEncoding(stringValue);
                } else if ("correlationId".equals(name)) {
                    propertiesBuilder.correlationId(stringValue);
                } else if ("replyTo".equals(name)) {
                    propertiesBuilder.replyTo(stringValue);
                } else if ("timestamp".equals(name)) {
                    try {
                        DateFormat formatter = new SimpleDateFormat("dd-MM-yyyy,HH:mm:ss SSS");
                        propertiesBuilder.timestamp(formatter.parse(stringValue));
                    } catch (ParseException e) {
                        log.error("{}", e);
                    }
                } else {
                    headers.put(name, value);
                }
            });
        }

        channel.basicPublish("", queueName, propertiesBuilder.headers(headers).build(), message.getBytes());
        channel.close();
    }

    @Override
    Connection getConnection() {
        throw new UnsupportedOperationException("getConnection unsupported in RabbitMqManager. RabbitMQ using custom implementation.");
    }

    @Override
    public void close() throws IOException {
        senderConnection.close();
    }

    @Override
    public Message waitMessage(String queueName, Long timeoutMs, String testIdHeaderName, String testId) throws JMSException {
        try {
            Channel channel = senderConnection.createChannel();
            final QueueingConsumer consumer = new QueueingConsumer(channel);
            channel.basicConsume(queueName, true, consumer);

            QueueingConsumer.Delivery delivery = consumer.nextDelivery(timeoutMs);
            if (delivery != null) {
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
                RMQTextMessage message = new RMQTextMessage();
                message.setText(new String(delivery.getBody()));
                return message;
            }
            return null;
        } catch (IOException | InterruptedException e) {
            log.error("RabbitMQ waitMessage error: {}", e);
        }
        return null;
    }
}
