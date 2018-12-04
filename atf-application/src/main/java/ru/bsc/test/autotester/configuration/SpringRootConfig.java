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

package ru.bsc.test.autotester.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;
import ru.bsc.test.at.util.YamlUtils;
import ru.bsc.test.autotester.properties.EnvironmentProperties;

import java.io.File;
import java.io.IOException;

/**
 * Created by sdoroshin on 21.03.2017.
 */
@Configuration
@ComponentScan("ru.bsc.test.autotester")
public class SpringRootConfig {

    @Bean
    public CommonsMultipartResolver multipartResolver() {
        CommonsMultipartResolver commonsMultipartResolver = new CommonsMultipartResolver();
        commonsMultipartResolver.setMaxUploadSize(50 * 1024 * 1024);
        commonsMultipartResolver.setMaxInMemorySize(1024 * 1024);
        return commonsMultipartResolver;
    }

    @Bean
    public EnvironmentProperties environmentProperties() throws IOException {
        return YamlUtils.loadAs(new File("env.yml"), EnvironmentProperties.class);
    }
}
