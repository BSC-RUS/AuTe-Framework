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

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by smakarov
 * 01.06.2018 16:41
 */
public class YamlUtilsTest {

    private static final String TEST_DATA_FILE = "tmp" + File.separator + "yaml-test.yml";

    private Yaml yaml = new Yaml();

    @After
    public void tearDown() throws Exception {
        clearTestFile();
    }

    @Test
    public void dumpToFileSuccess() throws IOException {
        TestObject testData = createTestData();
        clearTestFile();
        YamlUtils.dumpToFile(testData, TEST_DATA_FILE);
        Object loaded = yaml.load(FileUtils.readFileToString(Paths.get(TEST_DATA_FILE).toFile()));
        Assert.assertEquals(testData, loaded);
    }

    @Test
    public void loadAsFromStringSuccess() {
        TestObject testData = createTestData();
        String value = yaml.dump(testData);
        TestObject object = YamlUtils.loadAsFromString(value, TestObject.class);
        Assert.assertEquals(testData, object);
    }

    @Test
    public void loadAsSuccess() throws IOException {
        TestObject testData = createTestData();
        if (Files.exists(Paths.get(TEST_DATA_FILE))) {
            Files.delete(Paths.get(TEST_DATA_FILE));
        }
        File file = new File(TEST_DATA_FILE);
        FileUtils.writeStringToFile(file, yaml.dump(testData));
        TestObject loaded = YamlUtils.loadAs(file, TestObject.class);
        Assert.assertEquals(testData, loaded);
    }

    @Test
    public void loadAsEmptyFile() throws IOException {
        File file = new File(TEST_DATA_FILE);
        if (Files.exists(Paths.get(TEST_DATA_FILE))) {
            Files.delete(Paths.get(TEST_DATA_FILE));
        }
        FileUtils.writeStringToFile(file, "");
        TestObject object = YamlUtils.loadAs(file, TestObject.class);
        Assert.assertEquals(null, object);
    }

    private void clearTestFile() throws IOException {
        if (Files.exists(Paths.get(TEST_DATA_FILE))) {
            Files.delete(Paths.get(TEST_DATA_FILE));
        }
    }

    private TestObject createTestData() {
        List<String> list = new ArrayList<>();
        list.add("item1");
        list.add("item2");
        list.add("item3");
        return new TestObject("Str", 42, 42, 42.5, list);
    }
}