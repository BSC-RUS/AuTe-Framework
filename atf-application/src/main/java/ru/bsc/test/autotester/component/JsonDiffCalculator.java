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

package ru.bsc.test.autotester.component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bsc.test.autotester.diff.Diff;
import ru.bsc.test.autotester.diff.DiffMatchPatch;

import java.util.List;
import java.util.stream.Collectors;

import static ru.bsc.test.at.executor.utils.StreamUtils.nullSafeStream;

/**
 * Created by smakarov
 * 05.04.2018 12:19
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class JsonDiffCalculator {

    private final DiffMatchPatch dmp = new DiffMatchPatch();
    private final RequestResponseFormatter requestResponseFormatter;

    public List<Diff> calculate(String actual, String expected) {
        return dmp.diffMain(requestResponseFormatter.format(expected), requestResponseFormatter.format(actual));
    }

    public List<Diff> calculateAndTrim(String actual, String expected) {
        if (expected != null) {
            String[] lines = expected.split("\n");
            expected = nullSafeStream(lines).map(String::trim).collect(Collectors.joining(""));
        }
        return dmp.diffMain(requestResponseFormatter.format(expected), requestResponseFormatter.format(actual));
    }
}
