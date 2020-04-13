package ru.bsc.test.at.mock.mq.worker;

import com.ibm.mq.jms.MQQueueConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.commons.lang3.StringUtils;
import ru.bsc.test.at.mock.mq.components.MqProperties;
import ru.bsc.test.at.mock.mq.models.enums.MqBrokerType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

import static com.ibm.msg.client.wmq.common.CommonConstants.WMQ_CM_CLIENT;

@Slf4j
@RequiredArgsConstructor
public class MessageSender {
  private final MqProperties properties;

  public void send(String queue, String message) {
    try {
      if (properties.getManager() == MqBrokerType.ACTIVE_MQ) {
        sendActiveMq(queue, message);
      } else if (properties.getManager() == MqBrokerType.IBM_MQ) {
        sendIbmMq(queue, message);
      } else if (properties.getManager() == MqBrokerType.RABBIT_MQ) {
        sendRabbitMq(queue, message);
      }
    } catch (Exception e) {
      log.error("Unable send message", e);
    }
  }

  private void sendActiveMq(String queue, String text) throws JMSException {
    ConnectionFactory factory = new ActiveMQConnectionFactory(
        properties.getUsername(),
        properties.getPassword(),
        String.format("tcp://%s:%d", properties.getHost(), properties.getPort())
    );
    sendJms(queue, text, factory);
  }

  private void sendIbmMq(String queue, String text) throws JMSException {
    MQQueueConnectionFactory factory = new MQQueueConnectionFactory();
    factory.setHostName(properties.getHost());
    factory.setPort(properties.getPort());
    factory.setTransportType(WMQ_CM_CLIENT);
    if (StringUtils.isNotEmpty(properties.getChannel())) {
      factory.setChannel(properties.getChannel());
    }
    sendJms(queue, text, factory);
  }

  private void sendJms(String queue, String text, ConnectionFactory factory) throws JMSException {
    Connection connection = factory.createConnection();
    connection.start();
    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    Queue destination = session.createQueue(queue);
    MessageProducer producer = session.createProducer(destination);
    TextMessage message = session.createTextMessage();
    message.setText(text);
    producer.send(message);
    producer.close();
    session.close();
    connection.close();
  }

  private void sendRabbitMq(String queue, String text) throws IOException, TimeoutException {
    com.rabbitmq.client.ConnectionFactory factory = new com.rabbitmq.client.ConnectionFactory();
    factory.setHost(properties.getHost());
    factory.setPort(properties.getPort());
    factory.setUsername(properties.getUsername());
    factory.setPassword(properties.getPassword());
    com.rabbitmq.client.Connection connection = factory.newConnection();
    try (Channel channel = connection.createChannel()) {
      channel.basicPublish("", queue, MessageProperties.TEXT_PLAIN, text.getBytes(StandardCharsets.UTF_8));
    }
    connection.close();
  }
}
