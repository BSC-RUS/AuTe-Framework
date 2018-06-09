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

package ru.bsc.test.autotester.report.impl.allure.attach.builder.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.bsc.test.at.executor.model.RequestData;
import ru.bsc.test.autotester.report.impl.allure.attach.builder.AttachBuilder;
import ru.bsc.test.autotester.report.impl.allure.attach.extract.AttachExtractor;

import java.util.List;

/**
 * Created by smakarov
 * 30.03.2018 12:28
 */
@Component
public class RequestDataAttachBuilder extends AttachBuilder<RequestData> {

    @Autowired
    public RequestDataAttachBuilder(List<AttachExtractor<RequestData>> extractors) {
        super(extractors);
    }
}
