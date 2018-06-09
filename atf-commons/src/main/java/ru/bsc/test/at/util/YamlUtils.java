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

package ru.bsc.test.at.util;

import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by sdoroshin on 03.11.2017.
 */
@Slf4j
public final class YamlUtils {
    private YamlUtils() {
    }

    public static void dumpToFile(Object data, String fileName) throws IOException {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setAnchorGenerator(new AutotesterAnchorGenerator());
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);

        File file = new File(fileName);
        if (!file.exists() && !file.getParentFile().mkdirs()) {
            log.info("Directory {} not created", file.getParentFile());
        }
        try (FileWriter fileWriter = new FileWriter(file)) {
            new Yaml(new SkipEmptyRepresenter(), dumperOptions).dump(data, fileWriter);
        }
    }

    public static <T> T loadAsFromString(String yamlContent, Class<T> type) {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        return new Yaml(representer).loadAs(yamlContent, type);
    }

    public static <T> T loadAs(File fileName, Class<T> type) throws IOException {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        try (FileReader fileReader = new FileReader(fileName)) {
            return new Yaml(representer).loadAs(fileReader, type);
        }
    }
}
