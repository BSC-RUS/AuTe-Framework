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

package ru.bsc.test.autotester.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.bsc.test.autotester.properties.AppProperties;
import ru.bsc.test.autotester.service.VersionService;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Tag;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.Collections;

/**
 * @author mobrubov
 * created on 11.01.2019 12:33
 */

@Configuration
@EnableSwagger2
public class SpringfoxConfig {
    public static final String TAG_EXECUTION = "Test execution";
    public static final String TAG_PROJECT = "Project";
    public static final String TAG_SCENARIO = "Scenario";
    public static final String TAG_STEP = "Step";
    public static final String TAG_VERSION = "Version";

    @Bean
    Docket api(ApiInfo apiInfo) {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo)
                .protocols(Collections.singleton("http"))
                .select()
                    .apis(RequestHandlerSelectors.any())
                    .paths(PathSelectors.ant("/rest/**"))
                    .build()
                .useDefaultResponseMessages(false)
                .tags(
                        new Tag(TAG_EXECUTION, "Executing auto-tests"),
                        new Tag(TAG_PROJECT, "Operations with projects and scenario groups"),
                        new Tag(TAG_SCENARIO, "Operations with scenarios and its steps"),
                        new Tag(TAG_STEP, "Operations with steps"),
                        new Tag(TAG_VERSION, "Components versions")
                );
    }

    @Bean
    ApiInfo apiInfo(AppProperties appProperties, VersionService versionService) {
        return new ApiInfoBuilder()
                .title(appProperties.getTitle())
                .description(appProperties.getDescription())
                .version(versionService.getApplicationVersion().getImplementationVersion())
                .contact(new Contact(
                        appProperties.getContact().getName(),
                        appProperties.getContact().getUrl(),
                        appProperties.getContact().getEmail()
                ))
                .license(appProperties.getLicense().getName())
                .licenseUrl(appProperties.getLicense().getUrl())
                .build();
    }
}
