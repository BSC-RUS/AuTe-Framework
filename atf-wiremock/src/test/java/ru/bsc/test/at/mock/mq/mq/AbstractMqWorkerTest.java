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

package ru.bsc.test.at.mock.mq.mq;

import com.anarsoft.vmlens.concurrent.junit.ConcurrentTestRunner;
import com.anarsoft.vmlens.concurrent.junit.ThreadCount;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import ru.bsc.test.at.mock.mq.models.MockMessage;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeoutException;

@RunWith(ConcurrentTestRunner.class)
public class AbstractMqWorkerTest {


    private AbstractMqWorker testWorker;
    private final List<MockMessage> list = new LinkedList<>();

    @Before
    public void init() {

        MockMessage mockMessage = new MockMessage();
        mockMessage.setSourceQueueName("queue4");
        mockMessage.setTestId("testId");
        list.add(mockMessage);

        mockMessage = new MockMessage();
        mockMessage.setSourceQueueName("queue3");
        mockMessage.setTestId("testId");
        list.add(mockMessage);

        mockMessage = new MockMessage();
        mockMessage.setSourceQueueName("queue2");
        mockMessage.setTestId("testId");
        list.add(mockMessage);

        mockMessage = new MockMessage();
        mockMessage.setSourceQueueName("queue1");
        mockMessage.setTestId("testId");
        list.add(mockMessage);

        mockMessage = new MockMessage();
        mockMessage.setSourceQueueName("queue");
        mockMessage.setTestId("testId");
        list.add(mockMessage);

        testWorker = new TestAbstractMqWorker("queue", null, list, null, null, null, null);
    }

    @Test()
    @ThreadCount(1000)
    public void testFindMockMessageConcurrentAccess() {
        for (int i = 0; i < 10; i++) {
            MockMessage message = testWorker.findMockMessage("testId", "<?xml version=\"1.0\" encoding=\"UTF-8\"?><note></note>");
            synchronized (list) {

                MockMessage mockMessage = new MockMessage();
                mockMessage.setSourceQueueName("q");
                mockMessage.setTestId("t");

                list.add(mockMessage);
            }
            Assert.assertNotNull(message);
        }
    }

    class TestAbstractMqWorker extends AbstractMqWorker {
        TestAbstractMqWorker(String queueNameFrom, String queueNameTo, List<MockMessage> mockMappingList, String brokerUrl, String username, String password, String testIdHeaderName) {
            super(queueNameFrom, queueNameTo, mockMappingList, brokerUrl, username, password, testIdHeaderName);
        }

        @Override
        void runWorker() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void stop() throws IOException, TimeoutException {
            throw new UnsupportedOperationException();
        }
    }
}