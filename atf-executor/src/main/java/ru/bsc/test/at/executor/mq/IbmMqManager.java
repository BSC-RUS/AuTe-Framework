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

import com.ibm.mq.jms.MQQueueConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.QueueConnection;

@Slf4j
public class IbmMqManager extends AbstractMqManager {

    private QueueConnection connection;

    IbmMqManager(String host, int port, String username, String password) throws JMSException {
        MQQueueConnectionFactory connectionFactory = new MQQueueConnectionFactory();
        connectionFactory.setHostName(host);
        connectionFactory.setPort(port);
        connectionFactory.setTransportType(1);
        connection = (QueueConnection) connectionFactory.createConnection(username, password);
        connection.start();
    }

    @Override
    Connection getConnection() {
        return connection;
    }

    @Override
    public void close() {
        try {
            connection.close();
        } catch (JMSException e) {
            log.error("{}", e);
        }
    }
}
