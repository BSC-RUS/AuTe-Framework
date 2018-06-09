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
import java.util.jar.JarFile;

import static java.lang.Thread.currentThread;
import static org.apache.commons.lang3.StringUtils.isEmpty;

/**
 * Created by smakarov
 * 26.02.2018 15:26
 */

@Getter
@ToString
@AllArgsConstructor
@Slf4j
public class Version {
    private final String implementationVersion;
    private final String implementationDate;

    public static Version load() {
        ClassLoader classLoader = currentThread().getContextClassLoader();
        logManifestPaths();
        try (InputStream manifest = classLoader.getResourceAsStream(JarFile.MANIFEST_NAME)) {
            Properties properties = new Properties();
            properties.load(manifest);
            return buildVersionFromProperties(properties);
        } catch (IOException e) {
            log.error("Error while reading MANIFEST.MF", e);
            return Version.unknown();
        }
    }

    public static Version unknown() {
        return new Version();
    }

    /**
     * @see "COM-270"
     */
    private static void logManifestPaths() {
        String path = JarFile.MANIFEST_NAME;
        String absolutePath = "/" + path;
        ClassLoader classLoader = Version.class.getClassLoader();
        ClassLoader contextClassLoader = currentThread().getContextClassLoader();
        log.debug("MANIFEST paths check:");
        log.debug("  Class, relative: '{}'", Version.class.getResource(path));
        log.debug("  Class, absolute: '{}'", Version.class.getResource(absolutePath));
        log.debug("  Class loader, relative: '{}'", classLoader.getResource(path));
        log.debug("  Class loader, absolute: '{}'", classLoader.getResource(absolutePath));
        log.debug("  Context class loader, relative: '{}'", contextClassLoader.getResource(path));
        log.debug("  Context class loader, absolute: '{}'", contextClassLoader.getResource(absolutePath));
    }

    private static Version buildVersionFromProperties(Properties properties) {
        log.debug("MANIFEST.MF properties");
        properties.forEach((k, v) -> log.debug("{}: {}", k, v));
        String projectVersion = properties.getProperty("Project-Version");
        String commitsCount = properties.getProperty("Git-Commits-Count");
        String shortRevision = properties.getProperty("Git-Short-Revision");
        if (isEmpty(projectVersion) || isEmpty(commitsCount) || isEmpty(shortRevision)) {
            log.warn("Version information is not available. Build the project using maven.");
            return Version.unknown();
        }
        String implementationVersion = String.format("%s.%s.%s", projectVersion, commitsCount, shortRevision);
        String implementationDate = properties.getProperty("Build-Time");
        Version version = new Version(implementationVersion, implementationDate);
        log.info("Version loaded from MANIFEST.MF: {}", version);
        return version;
    }

    public Version() {
        this("unknown", "");
    }
}
