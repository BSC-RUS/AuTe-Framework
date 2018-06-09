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

package ru.bsc.test.autotester.ro;

import lombok.Getter;
import lombok.Setter;
import ru.bsc.test.autotester.diff.Diff;

import java.util.List;

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
public class StepResultRo {
    private String testId;
    private StepRo step;
    private String result;
    private String details;
    private String expected;
    private List<Diff> diff;
    private String actual;
    private String requestUrl;
    private String requestBody;
    private Integer pollingRetryCount;
    private String savedParameters;
    private String description;
    private boolean editable;
    private String cookies;
    private List<String> sqlQueryList;

    private List<RequestDataRo> requestDataList;
}
