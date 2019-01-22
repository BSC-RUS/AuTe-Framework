/*
 *
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the ATF project
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

package ru.bsc.test.at.executor.service;

import lombok.extern.slf4j.Slf4j;
import ru.bsc.test.at.executor.model.MockServiceResponse;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.step.executor.ParsedMockResponseBody;

import java.util.Objects;

@Slf4j
public class DelayUtilities {
    public void waitWiremockDelayFromGroovyScripts(Step step) {
        if (step == null || step.getMockServiceResponseList() == null) {
            return;
        }
        long summaryDelay = step.getMockServiceResponseList().stream()
                .filter(Objects::nonNull)
                .map(MockServiceResponse::getResponseBody)
                .filter(Objects::nonNull)
                .map(ParsedMockResponseBody::new)
                .mapToLong(ParsedMockResponseBody::groovyDelay)
                .sum();
        delay(summaryDelay);
    }

    public void delay(long timeout) {
        try {
            Thread.sleep(Math.max(0, timeout));
        } catch (InterruptedException ex) {
            log.error("Can't interrupt thread for delay", ex);
        }
    }
}
