/*
 * Diff Match and Patch
 * Copyright 2018 The diff-match-patch Authors.
 * https://github.com/google/diff-match-patch
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.bsc.test.autotester.diff;

import java.util.List;

/**
 * Internal class for returning results from diffLinesToChars().
 * Other less paranoid languages just use a three-element array.
 */
@SuppressWarnings("all")
public class LinesToCharsResult {

    protected String chars1;
    protected String chars2;
    protected List<String> lineArray;

    protected LinesToCharsResult(String chars1, String chars2,
                                 List<String> lineArray) {
        this.chars1 = chars1;
        this.chars2 = chars2;
        this.lineArray = lineArray;
    }
}
