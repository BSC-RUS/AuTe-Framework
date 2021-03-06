/*
 * Copyright 2018 BSC Msc, LLC
 *
 * This file is part of the ATF project
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

package ru.bsc.test.autotester.ro;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;
import ru.bsc.test.autotester.diff.Diff;

import java.util.List;

@Getter
@Setter
@ApiModel
public class ExpectedServiceRequestResultRo {
	private boolean expected;
	private String expectedRequest;
	private RequestResultRo actualRequest;
	private List<Diff> diff;
	private String actualDiff;
	private String expectedDiff;
	private boolean hasDiff;
}
