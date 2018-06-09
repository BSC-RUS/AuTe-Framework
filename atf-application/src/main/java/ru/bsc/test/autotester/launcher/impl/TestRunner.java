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

package ru.bsc.test.autotester.launcher.impl;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import ru.bsc.test.autotester.launcher.api.TestLauncher;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by smakarov
 * 23.03.2018 10:24
 */
@Component
@Slf4j
public class TestRunner implements CommandLineRunner {

    @Autowired
    private TestLauncher launcher;

    @Override
    public void run(String... args) {
        if (Arrays.asList(args).contains("execute")) {
            Set<String> loggers = new HashSet<>(Arrays.asList(
                    "org.apache.http",
                    "org.apache.commons.beanutils.converters"
            ));

            for (String log : loggers) {
                Logger logger = (Logger) LoggerFactory.getLogger(log);
                logger.setLevel(Level.WARN);
                logger.setAdditive(false);
            }

            log.info("Running tests");
            try {
                launcher.launch();
            } catch (Exception e) {
                log.error("Error while running tests", e);
            }
        }
    }
}
