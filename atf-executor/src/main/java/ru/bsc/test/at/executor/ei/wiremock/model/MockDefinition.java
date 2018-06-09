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

package ru.bsc.test.at.executor.ei.wiremock.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;

/**
 * Created by sdoroshin on 27.07.2017.
 *
 */
@NoArgsConstructor
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MockDefinition {
    private String id;
    private String uuid;
    private Long priority;
    private MockRequest request;
    private MockResponse response;

    public MockDefinition(Long priority, String testIdHeaderName, String testId) {
        this.priority = priority;
        request = new MockRequest();
        HashMap<String, String> equalTo = new HashMap<>();
        equalTo.put("equalTo", testId);
        request.getHeaders().put(testIdHeaderName, equalTo);
        response = new MockResponse();
    }
}
