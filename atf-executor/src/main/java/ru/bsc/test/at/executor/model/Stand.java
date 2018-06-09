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

import lombok.Data;

import java.io.Serializable;

/**
 * Created by sdoroshin on 12.07.2017.
 */
@Data
public class Stand implements AbstractModel, Serializable {
    private static final long serialVersionUID = 1616628096472011795L;

    private String serviceUrl;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private String wireMockUrl;

    public Stand copy() {
        Stand stand = new Stand();
        stand.setServiceUrl(getServiceUrl());
        stand.setDbUrl(getDbUrl());
        stand.setDbUser(getDbUser());
        stand.setDbPassword(getDbPassword());
        stand.setWireMockUrl(getWireMockUrl());
        return stand;
    }
}
