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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by sdoroshin on 03.11.2017.
 */
@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlUtils {
    private static final Charset CHARSET = StandardCharsets.UTF_8;

    public static <T> boolean saveToFile(T data, Path path) {
        try {
            if (Files.notExists(path.getParent())) {
                Files.createDirectory(path.getParent());
            }
        } catch (IOException e) {
            log.error("Unable to create {} dir", path.getParent(), e);
            return false;
        }
        try (BufferedWriter writer = Files.newBufferedWriter(path, CHARSET)) {
            new Yaml(new SkipEmptyRepresenter(), getDumperOptions()).dump(data, writer);
            return true;
        } catch (IOException e) {
            log.error("Error while saving JMS mappings to file", e);
            return false;
        }
    }

    public static <T> T loadAs(File fileName, Class<T> type) throws IOException {
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        try (FileReader fileReader = new FileReader(fileName)) {
            return new Yaml(representer).loadAs(fileReader, type);
        }
    }

    public static <T> Optional<T> loadAs(Path path, Class<T> type) throws IOException {
        if (Files.notExists(path)) {
            return Optional.empty();
        }
        Representer representer = new Representer();
        representer.getPropertyUtils().setSkipMissingProperties(true);
        try (BufferedReader reader = Files.newBufferedReader(path, CHARSET)) {
            return Optional.ofNullable(new Yaml(representer).loadAs(reader, type));
        }
    }

    private static DumperOptions getDumperOptions() {
        DumperOptions dumperOptions = new DumperOptions();
        dumperOptions.setAnchorGenerator(new AutotesterAnchorGenerator());
        dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        return dumperOptions;
    }
}
