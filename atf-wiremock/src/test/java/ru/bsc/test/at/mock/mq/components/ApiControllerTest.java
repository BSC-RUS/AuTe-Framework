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

import org.apache.commons.collections.Buffer;
import org.apache.commons.collections.BufferUtils;
import org.apache.commons.collections.buffer.CircularFifoBuffer;
import org.junit.Assert;
import org.junit.Test;
import ru.bsc.test.at.mock.mq.models.MockedRequest;

import java.lang.reflect.Field;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collection;

public class ApiControllerTest {

    public static final int BUFF_SIZE = 1000;
    private ApiController apiController;

    public ApiControllerTest() throws NoSuchFieldException, IllegalAccessException {

        MqRunnerComponent mqRunnerComponent = new MqRunnerComponent();
        Field fifo =  MqRunnerComponent.class.getDeclaredField("fifo");
        fifo.setAccessible(true);
        fifo.set(mqRunnerComponent, BufferUtils.synchronizedBuffer(new CircularFifoBuffer(BUFF_SIZE)));



        for(int i = 0; i < BUFF_SIZE; i++) {
            MockedRequest mockedRequest = new MockedRequest();
            mockedRequest.setDate(Date.from(LocalDateTime.of(2000+i,01,01,6,30).atZone(ZoneId.systemDefault()).toInstant()));
            Buffer b = mqRunnerComponent.getFifo();
            b.add(mockedRequest);
        }

        apiController = new ApiController(mqRunnerComponent);
    }


    @Test()
    public void getRequestListSizeTest(){
        Collection requestList = apiController.getRequestList(null);
        Assert.assertTrue(requestList.size() == BUFF_SIZE);
        requestList = apiController.getRequestList(BUFF_SIZE);
        Assert.assertTrue(requestList.size() == BUFF_SIZE);
        requestList = apiController.getRequestList(5);
        Assert.assertTrue(requestList.size() == 5);

    }
}
