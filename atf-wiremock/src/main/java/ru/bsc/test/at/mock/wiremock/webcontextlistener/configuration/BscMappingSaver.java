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

package ru.bsc.test.at.mock.wiremock.webcontextlistener.configuration;

import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.stubbing.StubMapping;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

/**
 * Created by sdoroshin on 04.08.2017.
 *
 */
@Slf4j
class BscMappingSaver implements MappingsSaver {
    private static final String SAFE_CHARACTERS_PATTERN = "[^А-Яа-яa-zA-Z0-9.+\\-() ]";

    @Override
    public void save(List<StubMapping> mappings) {
        try {
            final Path mappingsPath = Paths.get("mappings");
            clearDirectory(mappingsPath);
            for (StubMapping m : mappings) {
                Files.write(resolveMappingPath(mappingsPath, m), getMappingContent(m));
            }
        } catch (IOException e) {
            log.error("Error while saving mappings",e);
            throw new RuntimeException(e);
        }
        log.trace("Save list: {}", mappings.toString());
    }

    @Override
    public void save(StubMapping stubMapping) {
        log.trace("Save one: {}", stubMapping.toString());
    }

    @Override
    public void remove(StubMapping stubMapping) {
        log.trace("Remove: {}", stubMapping.toString());
    }

    @Override
    public void removeAll() {
        log.trace("Remove all");
    }

    private void clearDirectory(Path mappingsPath) throws IOException {
        boolean filesRemoved = Files.walk(mappingsPath)
            .sorted(Comparator.reverseOrder())
            .map(Path::toFile)
            .allMatch(File::delete);
        if (!filesRemoved) {
            throw new IllegalStateException("Cannot clean mapping directory");
        }
    }

    private Path resolveMappingPath(Path root, StubMapping mapping) throws IOException {
        Path directory = mapping.getScenarioName() != null ? root.resolve(safeName(mapping.getScenarioName())) : root;
        if (Files.notExists(directory)) {
            Files.createDirectories(directory);
        }
        String name = mapping.getName() != null ? safeName(mapping.getName()) : mapping.getUuid().toString();
        return directory.resolve(name + ".json").normalize();
    }

    private byte[] getMappingContent(StubMapping m) {
        return m.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String safeName(String name) {
        return name.replaceAll(SAFE_CHARACTERS_PATTERN, "_");
    }
}
