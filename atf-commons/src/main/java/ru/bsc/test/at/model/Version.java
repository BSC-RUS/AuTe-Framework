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

package ru.bsc.test.at.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static java.lang.Thread.currentThread;

/**
 * Created by smakarov
 * 26.02.2018 15:26
 */

@Getter
@ToString
@AllArgsConstructor
@Slf4j
public class Version {

    private static final String VERSION_FILE = ".version.properties";

    private final String implementationVersion;
    private final String implementationDate;

    public static Version load(String applicationName) {
        String versionFile = applicationName + VERSION_FILE;
        try (InputStream resource = currentThread().getContextClassLoader().getResourceAsStream(versionFile)) {
            Properties properties = new Properties();
            properties.load(resource);
            String implementationVersion = String.format(
                    "%s.%s.%s.%s",
                    properties.getProperty("git.build.version"),
                    properties.getProperty("git.branch"),
                    properties.getProperty("git.total.commit.count"),
                    properties.getProperty("git.commit.id.abbrev")
            );
            String implementationDate = properties.getProperty("git.build.time");
            Version version = new Version(implementationVersion, implementationDate);
            log.info("Version loaded from file: {}", version);
            return version;
        } catch (IOException e) {
            log.error("Error while loading '{}' properties", versionFile, e);
            return Version.unknown();
        }
    }

    public static Version unknown() {
        return new Version();
    }

    public Version() {
        this("unknown", "");
    }
}
