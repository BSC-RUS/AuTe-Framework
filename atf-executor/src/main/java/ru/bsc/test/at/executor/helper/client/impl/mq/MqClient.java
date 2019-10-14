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

package ru.bsc.test.at.executor.helper.client.impl.mq;

import lombok.extern.slf4j.Slf4j;
import ru.bsc.test.at.executor.helper.client.api.Client;
import ru.bsc.test.at.executor.helper.client.api.ClientCommonResponse;
import ru.bsc.test.at.executor.helper.client.api.ClientResponse;
import ru.bsc.test.at.executor.model.AmqpBroker;
import ru.bsc.test.at.executor.mq.AbstractMqManager;
import ru.bsc.test.at.executor.mq.MqManagerFactory;

import javax.jms.BytesMessage;
import javax.jms.Message;
import javax.jms.TextMessage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Slf4j
public class MqClient implements Client<ClientMQRequest, MqClient.ClientVoidResponse> {

    private final AbstractMqManager mqManager;

    private long maxTimeoutWait = 60000L;
    private boolean useCamelNamingPolicyIbmMQ;

    public MqClient(AmqpBroker amqpBroker) throws Exception {
        mqManager = MqManagerFactory.getMqManager(
                amqpBroker.getMqService(),
                amqpBroker.getHost(),
                amqpBroker.getPort(),
                amqpBroker.getUsername(),
                amqpBroker.getPassword(),
                amqpBroker.getChannel()
        );
        setMaxTimeoutWait(amqpBroker.getMaxTimeoutWait());
        setUseCamelNamingPolicyIbmMQ(amqpBroker.isUseCamelNamingPolicyIbmMQ());
    }


    public long getMaxTimeoutWait() {
        return maxTimeoutWait;
    }

    public void setMaxTimeoutWait(long maxTimeoutWait) {
        if(maxTimeoutWait > 0) {
            this.maxTimeoutWait = maxTimeoutWait;
        }
    }

    public boolean isUseCamelNamingPolicyIbmMQ() {
        return useCamelNamingPolicyIbmMQ;
    }

    public void setUseCamelNamingPolicyIbmMQ(boolean useCamelNamingPolicyIbmMQ) {
        this.useCamelNamingPolicyIbmMQ = useCamelNamingPolicyIbmMQ;
    }


    @Override
    public ClientVoidResponse request(ClientMQRequest request) throws Exception {
        mqManager.sendTextMessage(request.getResource(), (String) request.getBody(), request.getHeaders(), request.getTestIdHeaderName(), request.getTestId());
        return new ClientVoidResponse();
    }

    public ClientCommonResponse waitMessage(String queueName, Long timeout, String testIdHeaderName, String testId) throws Exception {
        Message message = mqManager.waitMessage(queueName, timeout, testIdHeaderName, testId);

        if (message == null) {
            throw new Exception("No reply message");
        }

        if (message instanceof BytesMessage) {
            BytesMessage bytesMessage = (BytesMessage) message;
            byte[] data = new byte[(int) bytesMessage.getBodyLength()];
            bytesMessage.readBytes(data);
            return new ClientCommonResponse(0, new String(data, StandardCharsets.UTF_8), null);
        } else if (message instanceof TextMessage) {
            return new ClientCommonResponse(0, ((TextMessage) message).getText(), null);
        } else {
            throw new Exception("Received message is not TextMessage instance");
        }
    }

    @Override
    public void close() throws IOException {
        try {
            mqManager.close();
        } catch (IOException e) {
            log.error("Error closing MQ connection", e);
            throw e;
        }
    }

    static class ClientVoidResponse implements ClientResponse {
        @Override
        public int getStatusCode() {
            return 0;
        }

        @Override
        public String getContent() {
            return null;
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            return null;
        }
    }
}