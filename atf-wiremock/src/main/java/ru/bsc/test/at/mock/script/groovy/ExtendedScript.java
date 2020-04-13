package ru.bsc.test.at.mock.script.groovy;

import groovy.lang.Script;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.context.InternalContextAdapterImpl;
import ru.bsc.test.at.mock.mq.components.MqProperties;
import ru.bsc.test.at.mock.mq.worker.MessageSender;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
public abstract class ExtendedScript extends Script {
  public static final String MQ_PROPERTIES = "mq.properties";
  private static final int SLEEP_SECONDS = 3;

  @SuppressWarnings("unused")
  public void sendMessage(String queue, String text) {
    InternalContextAdapterImpl context = (InternalContextAdapterImpl) getProperty("context");
    if (context == null) {
      log.warn("Message not sent, unable to get context");
      return;
    }
    MqProperties properties = (MqProperties) context.get(MQ_PROPERTIES);
    if (properties == null || properties.isManagerNotDefined()) {
      log.warn("Message not sent, mq properties not defined");
      return;
    }
    CompletableFuture.runAsync(() -> {
      try {
        TimeUnit.SECONDS.sleep(SLEEP_SECONDS);
        new MessageSender(properties).send(queue, text);
      } catch (InterruptedException e) {
        log.error("Error", e);
      }
    });
  }
}
