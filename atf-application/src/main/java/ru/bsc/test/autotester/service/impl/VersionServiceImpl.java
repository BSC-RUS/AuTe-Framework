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

package ru.bsc.test.autotester.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import ru.bsc.test.at.model.Version;
import ru.bsc.test.autotester.properties.EnvironmentProperties;
import ru.bsc.test.autotester.properties.StandProperties;
import ru.bsc.test.autotester.service.VersionService;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Pavel Golovkin
 */
@Service
@Slf4j
public class VersionServiceImpl implements VersionService {
    private static final String WIREMOCK_VERSION_PATH = "__version";

    private final Version applicationVersion = Version.load("atf-application");
    private final List<WiremockVersion> wiremockVersions = new ArrayList<>();

    @Autowired
    public VersionServiceImpl(EnvironmentProperties env, RestTemplateBuilder builder) {
        loadProjectsWiremockVersion(env, builder);
    }

    @Override
    public Version getApplicationVersion() {
        return applicationVersion;
    }

    @Override
    public List<WiremockVersion> getWiremockVersions() {
        return wiremockVersions;
    }

    private void loadProjectsWiremockVersion(EnvironmentProperties env, RestTemplateBuilder builder) {
        RestTemplate template = builder.build();
        env.getProjectStandMap().forEach((projectName, properties) -> {
            try {
                if (StringUtils.isNotEmpty(properties.getWireMockUrl())) {
                    String url = getVersionUrl(properties);
                    Version version = template.getForObject(url, Version.class);
                    wiremockVersions.add(new WiremockVersion(projectName, version));
                }
            } catch (RestClientException e) {
                wiremockVersions.add(new WiremockVersion(projectName, Version.unknown()));
                log.error("Error while fetching project wiremock version", e);
            }
        });
    }

    private String getVersionUrl(StandProperties properties) {
        return properties.getWireMockUrl() +
                (properties.getWireMockUrl().endsWith("/") ? "" : "/") +
                WIREMOCK_VERSION_PATH;
    }
}
