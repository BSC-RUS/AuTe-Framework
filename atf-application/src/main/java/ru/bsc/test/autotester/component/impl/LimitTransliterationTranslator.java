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

package ru.bsc.test.autotester.component.impl;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import ru.bsc.test.autotester.component.Translator;

/**
 * Created by smakarov
 * 01.03.2018 15:11
 */
@Component
public class LimitTransliterationTranslator implements Translator {

    private static final String[] LATIN_ALPHABET_CHARS = new String[]{
            "a", "b", "v", "g", "d", "e", "yo", "g", "z", "i", "y", "i", "k", "l", "m",
            "n", "o", "p", "r", "s", "t", "u", "f", "h", "tz", "ch", "sh", "sh", "", "e", "yu", "ya"};
    private static final String CYRILLIC_ALPHABET = "абвгдеёжзиыйклмнопрстуфхцчшщьэюя";
    private static final int RESULT_MAX_LENGTH = 40;

    @Override
    public String translate(String text) {
        if (StringUtils.isBlank(text)) {
            return "";
        }
        StringBuilder translated = new StringBuilder("");
        for (char ch : text.toLowerCase().toCharArray()) {
            int charIndex = CYRILLIC_ALPHABET.indexOf(ch);
            translated.append(charIndex != -1 ? LATIN_ALPHABET_CHARS[charIndex] : ch);
        }
        String result = translated.toString().replaceAll("[^a-zA-Z0-9_-]", "-");
        return result.length() > RESULT_MAX_LENGTH ? result.substring(0, RESULT_MAX_LENGTH) : result;
    }
}
