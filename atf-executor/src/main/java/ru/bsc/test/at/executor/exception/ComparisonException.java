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

package ru.bsc.test.at.executor.exception;

import org.xmlunit.diff.Diff;

/**
 * Created by smakarov
 * 20.02.2018 10:52
 */
public class ComparisonException extends RuntimeException {
    public ComparisonException(Diff diff, String expectedRequest, String actualRequest) {
        super(String.format(
                "Service request error (request differences):%s\n\tExpected: %s\n\tActual: %s\n",
                diff,
                expectedRequest,
                actualRequest
        ));
    }
}
