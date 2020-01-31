package ru.bsc.test.at.mock.mq.models;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class MockMessageTest {
  @Test
  public void sortWithPriorityOnly() {
    List<MockMessage> messages = new ArrayList<>();
    messages.add(createMessageWithPriority(2));
    messages.add(createMessageWithPriority(1));
    messages.add(createMessageWithPriority(0));
    MockMessage message = messages.stream().sorted().findAny().orElse(null);
    assertNotNull(message);
    assertEquals(Integer.valueOf(0), message.getPriority());
  }

  @Test
  public void sortWithXpathAndTestId() {
    ArrayList<MockMessage> messages = new ArrayList<>();
    messages.add(createMessageWithTestIdAndXpath());
    messages.add(createMessageWithPriority(0));
    MockMessage message = messages.stream().sorted().findAny().orElse(null);
    assertNotNull(message);
    assertNotNull(message.getXpath());
    assertNotNull(message.getTestId());
  }

  @Test
  public void sortWithPriorityXpathAndTestId() {
    ArrayList<MockMessage> messages = new ArrayList<>();
    messages.add(createMessageWithPriorityAndTestIdAndXpath(2));
    messages.add(createMessageWithPriorityAndTestIdAndXpath(1));
    messages.add(createMessageWithPriorityAndTestIdAndXpath(0));
    MockMessage message = messages.stream().sorted().findAny().orElse(null);
    assertNotNull(message);
    assertEquals(Integer.valueOf(0), message.getPriority());
  }

  @Test
  public void sortWithPriorityAndXpath() {
    ArrayList<MockMessage> messages = new ArrayList<>();
    messages.add(createMessageWithPriority(2));
    messages.add(createMessageWithPriorityAndXpath(0));
    MockMessage message = messages.stream().sorted().findAny().orElse(null);
    assertNotNull(message);
    assertEquals(Integer.valueOf(0), message.getPriority());
  }

  private MockMessage createMessageWithPriority(Integer priority) {
    final MockMessage message = new MockMessage();
    message.setPriority(priority);
    return message;
  }

  private MockMessage createMessageWithTestIdAndXpath() {
    final MockMessage message = new MockMessage();
    message.setXpath("xpath");
    message.setTestId("testId");
    return message;
  }

  private MockMessage createMessageWithPriorityAndTestIdAndXpath(Integer priority) {
    final MockMessage message = new MockMessage();
    message.setPriority(priority);
    message.setXpath("xpath");
    message.setTestId("testId");
    return message;
  }

  private MockMessage createMessageWithPriorityAndXpath(Integer priority) {
    final MockMessage message = new MockMessage();
    message.setPriority(priority);
    message.setXpath("xpath");
    return message;
  }
}