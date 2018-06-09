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

package ru.bsc.test.at.mock.mq.components;

import lombok.Getter;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.models.PropertiesYaml;
import ru.bsc.test.at.mock.mq.models.enums.MqBrokerType;
import ru.bsc.test.at.mock.mq.mq.AbstractMqWorker;
import ru.bsc.test.at.mock.mq.mq.ActiveMQWorker;
import ru.bsc.test.at.mock.mq.mq.IbmMQWorker;
import ru.bsc.test.at.mock.mq.mq.RabbitMQWorker;
import ru.bsc.test.at.util.YamlUtils;

import javax.annotation.PostConstruct;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Component
public class MqRunnerComponent {

    @Value("${mq.manager}")
    private String mqManager;

    @Value("${mq.host}")
    private String mqHost;

    @Value("${mq.port}")
    private Integer mqPort;

    @Value("${mq.username}")
    private String mqUsername;

    @Value("${mq.password}")
    private String mqPassword;

    @Value("${mq.default.destination.queue.name}")
    private String defaultDestinationQueueName;

    @Value("${properties.yaml.file}")
    private String propertiesYamlFile;

    @Value("${test.id.header.name:testIdHeader}")
    private String testIdHeaderName;

    @Value("${mq.requestBufferSize:1000}")
    private int requestBufferSize;

    private final List<MockMessage> mockMappingList = new LinkedList<>();
    @Getter
    private Buffer fifo;
    private Map<String, AbstractMqWorker> queueListenerMap = new ConcurrentHashMap<>();

    private static void startListener(AbstractMqWorker mqWorker) {
        Thread brokerThread = new Thread(mqWorker);
        brokerThread.start();
    }

    String addMapping(MockMessage mockMessage) {
        synchronized (mockMappingList) {
            String sourceQueueName = mockMessage.getSourceQueueName();
            if (StringUtils.isEmpty(sourceQueueName)) {
                return null;
            }
            mockMessage.setGuid(UUID.randomUUID().toString());
            mockMappingList.add(mockMessage);

            if (!queueListenerMap.containsKey(sourceQueueName)) {
                AbstractMqWorker newMqListener = createMqBroker(sourceQueueName);
                queueListenerMap.put(sourceQueueName, newMqListener);
                startListener(newMqListener);
            }

            return mockMessage.getGuid();
        }
    }

    void deleteMapping(String mappingGuid) throws IOException, TimeoutException {
        synchronized (mockMappingList) {
            MockMessage mapping = mockMappingList
                    .stream()
                    .filter(mockMessage -> Objects.equals(mockMessage.getGuid(), mappingGuid)).findAny()
                    .orElse(null);
            mockMappingList.remove(mapping);

            if (mapping != null) {
                long count = mockMappingList.stream()
                        .filter(mockMessage -> Objects.equals(
                                mockMessage.getSourceQueueName(),
                                mapping.getSourceQueueName()
                        ))
                        .count();
                if (count == 0) {
                    AbstractMqWorker mqWorker = queueListenerMap.get(mapping.getSourceQueueName());
                    if (mqWorker != null) {
                        mqWorker.stop();
                        queueListenerMap.remove(mapping.getSourceQueueName());
                    }
                }
            }
        }
    }

    List<MockMessage> getMockMappingList() {
        synchronized (mockMappingList) {
            return mockMappingList;
        }
    }

    @PostConstruct
    public void initMappings() throws IOException {
        fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(requestBufferSize));
        if (!StringUtils.isEmpty(propertiesYamlFile)) {
            PropertiesYaml properties = YamlUtils.loadAs(new File(propertiesYamlFile), PropertiesYaml.class);
            if (properties != null && properties.getMockMessageList() != null) {
                properties.getMockMessageList().forEach(this::addMapping);
            }
        }
    }

    private MqBrokerType getBrokerType() {
        return MqBrokerType.valueOf(this.mqManager);
    }

    private AbstractMqWorker createMqBroker(String sourceQueueName) {
        switch (getBrokerType()) {
            case ACTIVE_MQ:
                return new ActiveMQWorker(
                        sourceQueueName,
                        defaultDestinationQueueName,
                        mockMappingList,
                        fifo,
                        mqHost,
                        mqUsername,
                        mqPassword,
                        testIdHeaderName
                );
            case RABBIT_MQ:
                return new RabbitMQWorker(
                        sourceQueueName,
                        defaultDestinationQueueName,
                        mockMappingList,
                        fifo,
                        mqHost,
                        mqUsername,
                        mqPassword,
                        mqPort,
                        testIdHeaderName
                );
            case IBM_MQ:
            default:
                return new IbmMQWorker(
                        sourceQueueName,
                        defaultDestinationQueueName,
                        mockMappingList,
                        fifo,
                        mqHost,
                        mqUsername,
                        mqPassword,
                        mqPort,
                        testIdHeaderName
                );
        }
    }
}
