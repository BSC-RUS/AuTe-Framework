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

import com.github.tomakehurst.wiremock.common.FileSource;
import com.github.tomakehurst.wiremock.core.MappingsSaver;
import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformer;
import com.github.tomakehurst.wiremock.servlet.WarConfiguration;
import com.google.common.base.Optional;
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import ru.bsc.velocity.directive.GroovyDirective;
import ru.bsc.velocity.directive.XPathDirective;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.ServletContext;

import static ru.bsc.test.at.mock.wiremock.Constants.VELOCITY_PROPERTIES;

/**
 * Created by sdoroshin on 04.08.2017.
 */
@Slf4j
public class CustomWarConfiguration extends WarConfiguration {
    private static final Integer DEFAULT_MAX_REQUEST_JOURNAL_ENTRIES = 250;

    private final String fileSourceRoot;
    private final ResponseDefinitionTransformer responseTransformer;

    public CustomWarConfiguration(ServletContext servletContext, String fileSourceRoot, ResponseDefinitionTransformer responseTransformer) {
        super(servletContext);
        this.fileSourceRoot = fileSourceRoot;
        this.responseTransformer = responseTransformer;
    }

    @Override
    public <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType) {
        if (!extensionType.equals(ResponseDefinitionTransformer.class)) {
            return Collections.emptyMap();
        }
        Map<String, T> transformers = new HashMap<>();
        transformers.put("velocity-response-transformer", (T) responseTransformer);
        return transformers;
    }

    @Override
    public FileSource filesRoot() {
        return new ServletContextFileSource(fileSourceRoot);
    }

    @Override
    public MappingsSaver mappingsSaver() {
        return new BscMappingSaver();
    }

    @Override
    @SuppressWarnings("Guava")
    public Optional<Integer> maxRequestJournalEntries() {
        return Optional.of(DEFAULT_MAX_REQUEST_JOURNAL_ENTRIES);
    }
}
