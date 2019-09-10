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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import ru.bsc.test.at.mock.mq.models.JmsMappings;
import ru.bsc.test.at.mock.mq.models.MockMessage;
import ru.bsc.test.at.mock.mq.models.enums.MqBrokerType;
import ru.bsc.test.at.mock.mq.worker.AbstractMqWorker;
import ru.bsc.test.at.mock.mq.worker.ActiveMQWorker;
import ru.bsc.test.at.mock.mq.worker.IbmMQWorker;
import ru.bsc.test.at.mock.mq.worker.RabbitMQWorker;
import ru.bsc.test.at.mock.wiremock.repository.JmsMappingsRepository;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

@Slf4j
@Component
@RequiredArgsConstructor
public class MqRunnerComponent {
    @Value("${mq.manager}")
    private String mqManager;

    @Value("${mq.host}")
    private String mqHost;

    @Value("${mq.port}")
    private Integer mqPort;

    @Value("${mq.channel:}")
    private String mqChannel;

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

    private final JmsMappingsRepository repository;

    private final List<MockMessage> mappings = new LinkedList<>();
    private final Map<String, AbstractMqWorker> queueListenerMap = new ConcurrentHashMap<>();

    @Getter
    private Buffer fifo;

    private static void startListener(AbstractMqWorker mqWorker) {
        Thread brokerThread = new Thread(mqWorker);
        brokerThread.start();
    }

    String addMapping(MockMessage mockMessage) {
        synchronized (mappings) {
            applyMapping(mockMessage);
            mockMessage.setGuid(UUID.randomUUID().toString());
            repository.save(new JmsMappings(mappings));
            return mockMessage.getGuid();
        }
    }

    void deleteMapping(String mappingGuid) throws IOException, TimeoutException {
        synchronized (mappings) {
            MockMessage mapping = mappings
                    .stream()
                    .filter(m -> Objects.equals(m.getGuid(), mappingGuid))
                    .findAny()
                    .orElse(null);
            mappings.remove(mapping);
            repository.save(new JmsMappings(mappings));

            if (mapping != null) {
                long count = mappings.stream()
                        .map(MockMessage::getSourceQueueName)
                        .filter(x -> Objects.equals(x, mapping.getSourceQueueName()))
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

    List<MockMessage> getMappings() {
        synchronized (mappings) {
            return mappings;
        }
    }

    @PostConstruct
    public void initMappings() {
        fifo = BufferUtils.synchronizedBuffer(new CircularFifoBuffer(requestBufferSize));
        repository.load().getMappings().forEach(this::applyMapping);
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
                        mappings,
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
                        mappings,
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
                        mappings,
                        fifo,
                        mqHost,
                        mqUsername,
                        mqPassword,
                        mqPort,
                        testIdHeaderName,
                        mqChannel
                );
        }
    }

    private void applyMapping(MockMessage mockMessage) {
        log.info("Applying JMS mapping: {}", mockMessage);
        String sourceQueueName = mockMessage.getSourceQueueName();
        if (StringUtils.isEmpty(sourceQueueName)) {
            log.warn("Source queue name not defined, skip mapping: {}", mockMessage.getGuid());
            throw new IllegalStateException("Source queue not defined");
        }

        mappings.add(mockMessage);
        if (!queueListenerMap.containsKey(sourceQueueName)) {
            AbstractMqWorker newMqListener = createMqBroker(sourceQueueName);
            queueListenerMap.put(sourceQueueName, newMqListener);
            startListener(newMqListener);
        }
    }

    void updateMapping(String guid, MockMessage mapping) {
        try {
            mapping.setGuid(guid);
            deleteMapping(guid);
            applyMapping(mapping);
            repository.save(new JmsMappings(mappings));
        } catch (IOException | TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
