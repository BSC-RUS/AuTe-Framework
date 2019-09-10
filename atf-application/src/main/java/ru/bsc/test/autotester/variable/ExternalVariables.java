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

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by smakarov
 * 25.07.2019 14:15
 */
@RequiredArgsConstructor
public class ExternalVariables {
    private final ApplicationArguments args;

    public Map<String, Map<String, String>> get() {
        Map<String, Map<String, String>> variables = new HashMap<>();
        variables.putAll(new GlobalVariables(args).parse());
        variables.putAll(new FileSourceVariables(args).read());
        return variables;
    }
}
