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

package ru.bsc.test.at.util;

/**
 * Created by lenovo on 20.02.2019.
 */
public enum MultipartConstant {

    CONVERT_BASE64_IN_MULTIPART("CONVERT_BASE64_IN_MULTIPART");

    private final String value;

    MultipartConstant(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

}
