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

package ru.bsc.test.at.executor.validation;

import java.util.regex.Pattern;

public final class MaskComparator {

    private static final String IGNORE = "*ignore*";

    private MaskComparator() { }

    public static boolean compare(String expected, String actual) {
        String preparedExpected = expected
                .replaceAll("[\n\r]+", "\n")
                .replaceAll(" ", " ")
                .trim();
        String preparedActual = actual
                .replaceAll("[\n\r]+", "\n")
                .replaceAll(" ", " ")
                .trim();
        String[] expectedParts = preparedExpected.split(Pattern.quote(IGNORE));
        int position = 0;
        String lastValue = null;
        for (String value : expectedParts) {
            int valuePos = preparedActual.indexOf(value, position);
            if (valuePos < 0 || valuePos < position) {
                return false;
            }
            position = valuePos + value.length();
            lastValue = value;
        }
        return preparedExpected.endsWith(IGNORE) || lastValue == null || preparedActual.endsWith(lastValue);
    }
}
