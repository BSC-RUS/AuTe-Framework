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

import java.util.List;

/**
 * Created by sdoroshin on 14.09.2017.
 */
@Getter
@Setter
public class MockServiceResponseRo implements AbstractRo {
    private static final long serialVersionUID = -7918346254164488513L;

    private String code;
    private String serviceUrl;
    private String httpMethod;
    private String responseBody;
    private String responseBodyFile;
    private Integer httpStatus;
    private String contentType;
    private String userName;
    private String password;
    private String pathFilter;
    private List<HeaderItemRo> headers;

}
