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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;
import ru.bsc.test.autotester.diff.Diff;
import ru.bsc.test.autotester.diff.DiffMatchPatch;

import java.util.List;

/**
 * Created by smakarov
 * 05.04.2018 12:19
 */
@Component
@Slf4j
public class JsonDiffCalculator {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private final DiffMatchPatch dmp = new DiffMatchPatch();
    private final JsonParser parser = new JsonParser();

    public List<Diff> calculate(String actual, String expected) {
        return dmp.diffMain(format(expected), format(actual));
    }

    private String format(String str){
        String value = str != null ? str : "";
        if(isJson(value)){
            try {
                JsonElement jsonElement = parser.parse(value);
                return gson.toJson(jsonElement);
            } catch (Exception e) {
                log.error("Error formatting string", e);
                return value;
            }
        }

        if (isXml(value)) {
            try {
                Document doc = Jsoup.parse(value);
                return doc.outerHtml();
            } catch (Exception e) {
                log.error("Error formatting string", e);
                return value;
            }
        }
        return value;
    }

    private boolean isJson(String str){
        String trimmed = str.trim();
        return trimmed.startsWith("{") && trimmed.endsWith("}") ||
               trimmed.startsWith("[") && trimmed.endsWith("]");

    }

    private boolean isXml(String str){
        String trimmed = str.trim();
        return trimmed.startsWith("<") && trimmed.endsWith(">");

    }
}
