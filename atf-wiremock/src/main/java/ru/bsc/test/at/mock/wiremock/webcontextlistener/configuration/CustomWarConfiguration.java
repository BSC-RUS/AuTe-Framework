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
import lombok.extern.slf4j.Slf4j;
import org.apache.velocity.app.Velocity;
import ru.bsc.test.at.mock.wiremock.transformers.CustomVelocityResponseTransformer;

import javax.servlet.ServletContext;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static ru.bsc.test.at.mock.wiremock.Constants.VELOCITY_PROPERTIES;

/**
 * Created by sdoroshin on 04.08.2017.
 *
 */
@Slf4j
public class CustomWarConfiguration extends WarConfiguration {
    private final String fileSourceRoot;

    public CustomWarConfiguration(ServletContext servletContext, String fileSourceRoot) {
        super(servletContext);
        this.fileSourceRoot = fileSourceRoot;
    }

    @Override
    public <T extends Extension> Map<String, T> extensionsOfType(Class<T> extensionType) {

        if (extensionType.equals(ResponseDefinitionTransformer.class)) {
            Map<String, T> transformers = new HashMap<>();

            // VelocityResponseTransformer configuration
            Properties properties = new Properties();
            properties.setProperty("userdirective", "ru.bsc.velocity.directive.XPathDirective,ru.bsc.velocity.directive.GroovyDirective");
            properties.setProperty("resource.loader", "file");
            properties.setProperty("file.resource.loader.path", "." + File.separator + "velocity");

            try (final InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(VELOCITY_PROPERTIES.getValue())) {
                properties.load(stream);
            } catch (IOException | NullPointerException e) {
                log.error("Error while loading properties. Using default properties", e);
            }
            Velocity.init(properties);

            transformers.put("velocity-response-transformer", (T) new CustomVelocityResponseTransformer());
            return transformers;
        }
        return Collections.emptyMap();
    }

    @Override
    public FileSource filesRoot() {
        return new ServletContextFileSource(fileSourceRoot);
    }

    @Override
    public MappingsSaver mappingsSaver() {
        return new BscMappingSaver();
    }
}
