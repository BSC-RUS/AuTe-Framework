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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.bsc.test.autotester.launcher.api.TestLauncher;
import ru.bsc.test.autotester.variable.ExternalVariables;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by smakarov
 * 23.03.2018 10:24
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TestRunner implements ApplicationRunner {
    private final TestLauncher launcher;

    @Override
    public void run(ApplicationArguments args) {
        if (args.getNonOptionArgs().contains("execute")) {
            prepareLoggers();
            try {
                launcher.launch(new ExternalVariables(args).get());
            } catch (Exception e) {
                log.error("Error while running tests", e);
            }
        }
    }

    private void prepareLoggers() {
        Set<String> loggers = new HashSet<>(Arrays.asList(
                "org.apache.http",
                "org.apache.commons.beanutils.converters"
        ));

        for (String log : loggers) {
            Logger logger = (Logger) LoggerFactory.getLogger(log);
            logger.setLevel(Level.WARN);
            logger.setAdditive(false);
        }
    }
}
