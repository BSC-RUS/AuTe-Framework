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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by smakarov
 * 25.07.2019 13:39
 */
public class GlobalVariables {
    private static final String GLOBAL_VARIABLE_ARG_NAME = "g";
    private static final char GLOBAL_VARIABLE_ARG_DIVIDER = ':';

    private final List<String> raw;

    GlobalVariables(ApplicationArguments args) {
        this.raw = args.getOptionValues(GLOBAL_VARIABLE_ARG_NAME);
    }

    Map<String, Map<String, String>> parse() {
        if (raw == null) {
            return Collections.emptyMap();
        }
        Map<String, Map<String, String>> variables = new HashMap<>();
        for(String rawVariable : raw) {
            int dividerPos = rawVariable.indexOf(GLOBAL_VARIABLE_ARG_DIVIDER);
            String projectCode = rawVariable.substring(0, dividerPos);
            variables.putIfAbsent(projectCode, new HashMap<>());

            dividerPos = rawVariable.indexOf(GLOBAL_VARIABLE_ARG_DIVIDER, dividerPos + 1);
            String varName = rawVariable.substring(projectCode.length() + 1, dividerPos);

            String varValue = rawVariable.substring(dividerPos + 1);

            variables.get(projectCode).put(varName, varValue);
        }
        return variables;
    }
}
