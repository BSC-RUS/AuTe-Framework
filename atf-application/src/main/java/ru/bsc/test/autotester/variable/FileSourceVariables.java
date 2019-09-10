/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

package ru.bsc.test.autotester.variable;

import org.springframework.boot.ApplicationArguments;
import ru.bsc.test.at.util.YamlUtils;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by smakarov
 * 25.07.2019 13:58
 */
public class FileSourceVariables {
    private static final String FILE_SOURCE_VARIABLE_NAME = "variables";
    private static final String NO_PATH = "";

    private final String filePath;

    FileSourceVariables(ApplicationArguments args) {
        final List<String> values = args.getOptionValues(FILE_SOURCE_VARIABLE_NAME);
        filePath = (values == null || values.size() != 1) ? NO_PATH : values.get(0);
    }

    @SuppressWarnings("unchecked")
    Map<String, Map<String, String>> read() {
        if (filePath.isEmpty()) {
            return Collections.emptyMap();
        }

        try {
            return YamlUtils.loadAs(Paths.get(filePath), Map.class).orElseThrow(() -> new RuntimeException("File not found"));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
