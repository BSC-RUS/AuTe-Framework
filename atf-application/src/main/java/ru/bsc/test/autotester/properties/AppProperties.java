/*
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

package ru.bsc.test.autotester.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author mobrubov
 * created on 16.01.2019 12:10
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "app")
public class AppProperties {
    private String title;
    private String description;
    private Contact contact;
    private License license;

    @Getter
    @Setter
    public static class Contact {
        private String name;
        private String url;
        private String email;
    }

    @Getter
    @Setter
    public static class License {
        private String name;
        private String url;
    }
}
