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

import javax.jms.JMSException;

public final class MqManagerFactory {

    private MqManagerFactory() { }

    public static AbstractMqManager getMqManager(MqService mqService, String host, Integer port, String username, String password, String channel) throws JMSException, ReflectiveOperationException {
        if (MqService.ACTIVE_MQ.equals(mqService)) {
            return new ActiveMqManager(host, port, username, password);
        } else if (MqService.RABBIT_MQ.equals(mqService)) {
            return new RabbitMqManager(host, port, username, password);
        } else if(MqService.IBM_MQ.equals(mqService)) {
            return new IbmMqManager(host, port, username, password, channel);
        } else {
            throw new UnsupportedOperationException("MqService " + mqService + " is not supported");
        }
    }
}
