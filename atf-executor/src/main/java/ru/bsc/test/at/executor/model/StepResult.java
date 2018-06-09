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

package ru.bsc.test.at.executor.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by sdoroshin on 12.05.2017.
 */
@Getter
@Setter
@NoArgsConstructor
public class StepResult {

    private String testId;
    private String projectCode;
    private Step step;
    private StepResultType result;
    private String details;
    private String expected;
    private String actual;
    private String requestUrl;
    private String requestBody;
    private Integer pollingRetryCount;
    private String savedParameters;
    private String description;
    private boolean editable;
    private long start;
    private long stop;
    private String cookies;
    private List<RequestData> requestDataList;
    private Map<String, Object> scenarioVariables;
    private List<String> sqlQueryList;

    public StepResult(String projectCode, Step step) {
        this.projectCode = projectCode;
        this.step = step;
    }

    public enum StepResultType {
        OK("OK", true),
        FAIL("Fail", false);

        String text;
        boolean isPositive;

        StepResultType(String text, boolean isPositive) {
            this.text = text;
            this.isPositive = isPositive;
        }

        public String getText() {
            return text;
        }

        public boolean isPositive() {
            return isPositive;
        }
    }
}


