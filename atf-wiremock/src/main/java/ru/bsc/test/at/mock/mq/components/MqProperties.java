package ru.bsc.test.at.mock.mq.components;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.mock.mq.models.enums.MqBrokerType;

@Getter
@Setter
@Component
@ConfigurationProperties("mq")
public class MqProperties {
  @Value("${manager:}")
  private MqBrokerType manager;

  @Value("${host:localhost}")
  private String host;

  @Value("${port:1398}")
  private Integer port;

  @Value("${channel:}")
  private String channel;

  @Value("${username:}")
  private String username;

  @Value("${password:}")
  private String password;

  @Value("${default.destination.queue.name:}")
  private String defaultDestinationQueueName;

  @Value("${requestBufferSize:1000}")
  private int requestBufferSize;

  public boolean isManagerNotDefined() {
    return manager == null;
  }
}
