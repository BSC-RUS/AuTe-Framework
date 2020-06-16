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

package ru.bsc.test.at.executor.model;

import lombok.*;
import ru.bsc.test.at.executor.ei.wiremock.model.MockedRequest;
import ru.bsc.test.at.executor.ei.wiremock.model.WireMockRequest;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpectedRequestResult {

	private boolean expected;
	private String expectedRequest;
	private RequestResult actualRequest;
	private boolean hasDiff;

	public boolean equalsActualRequest(MockedRequest actual) {
		return actualRequest != null && actualRequest.getBody() != null && actual != null && actualRequest.getBody().equals(actual.getRequestBody());
	}

	public boolean equalsActualRequest(WireMockRequest actual) {
		return actualRequest != null && actualRequest.getBody() != null && actual != null && actualRequest.getBody().equals(actual.getBody());
	}

}
