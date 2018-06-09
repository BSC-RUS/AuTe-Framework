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
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Created by sdoroshin on 04.08.2017.
 *
 */
@Slf4j
class BscMappingSaver implements MappingsSaver {
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss-SSS", Locale.ENGLISH);

    @Override
    public void save(List<StubMapping> stubMappings) {
        String mappingPath = ".";
        File mappingActual = new File(mappingPath + "/mappings/");
        File newName = new File(mappingPath + "/mappings_" + dateFormat.format(Calendar.getInstance().getTime()) + "/");
        boolean isRenamed = mappingActual.renameTo(newName);
        if (isRenamed) {
            log.info("File {} is renamed", mappingActual);
        } else {
            log.warn("File {} not renamed", mappingActual);
        }

        try {
            for (StubMapping stubMapping: stubMappings) {
                String fileName = "";
                if (stubMapping.getRequest().getUrl() != null) {
                    fileName = stubMapping.getRequest().getUrl().replaceAll("[^\\\\/a-zA-Z0-9.-]", "_");
                } else if (stubMapping.getRequest().getUrlPattern() != null) {
                    fileName = stubMapping.getRequest().getUrlPattern().replaceAll("[^\\\\/a-zA-Z0-9.-]", "_");
                }
                // Replace to: "/mappings/"
                File file = new File(mappingActual, fileName);
                //noinspection ResultOfMethodCallIgnored
                file.mkdirs();
                Files.write(stubMapping.toString(), new File(file, stubMapping.getUuid() + ".json"), StandardCharsets.UTF_8);
            }
            FileUtils.deleteDirectory(newName);
        } catch (IOException e) {
            log.error("Error while save subMapping list", e);
        }
        log.info("Save list: {}", stubMappings.toString());
    }

    @Override
    public void save(StubMapping stubMapping) {
        log.info("Save one: {}", stubMapping.toString());
    }

    @Override
    public void remove(StubMapping stubMapping) {
        log.info("Remove: {}", stubMapping.toString());
    }

    @Override
    public void removeAll() {
        log.info("Remove all");
    }
}
