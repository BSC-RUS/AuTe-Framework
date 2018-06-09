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

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.ReflectionUtils;

import javax.jms.*;
import java.io.IOException;
import java.lang.reflect.Method;

@Slf4j
public class IbmMqManager extends AbstractMqManager {

    private QueueConnection connection;

    IbmMqManager(String host, int port, String username, String password) throws JMSException, ReflectiveOperationException {
        ConnectionFactory connectionFactory = fillConnectionFactory(host, port);
        connection = (QueueConnection) connectionFactory.createConnection(username, password);
        connection.start();
    }

    @Override
    Connection getConnection() {
        return connection;
    }

    private ConnectionFactory fillConnectionFactory(String host, int port) throws ReflectiveOperationException {
        ConnectionFactory connectionFactory;
        try {
            connectionFactory = (QueueConnectionFactory) Class.forName("com.ibm.mq.jms.MQQueueConnectionFactory").newInstance();
        } catch (ClassNotFoundException e) {
            log.error("Error creating connection factory", e);
            throw new ClassNotFoundException(e.getMessage() + ": set class path for library for Ibm Mq provider", e);
        }

        invoke(connectionFactory, "setHostName", host);
        invoke(connectionFactory, "setPort", port);
        invoke(connectionFactory, "setTransportType", 1);

        return connectionFactory;
    }

    private void invoke(Object obj, String methodName, Object val) throws ReflectiveOperationException {
        Method method;
        if (val instanceof Integer) {
            method = ReflectionUtils.findMethod(obj.getClass(), methodName, int.class);
        } else {
            method = ReflectionUtils.findMethod(obj.getClass(), methodName, val.getClass());
        }

        method.invoke(obj, val);
    }

    @Override
    public void close() throws IOException {
        try {
            connection.close();
        } catch (JMSException e) {
            log.error("{}", e);
        }
    }
}
