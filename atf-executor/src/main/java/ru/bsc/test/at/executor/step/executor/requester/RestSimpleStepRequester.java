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

package ru.bsc.test.at.executor.step.executor.requester;

import lombok.extern.slf4j.Slf4j;
import ru.bsc.test.at.executor.helper.client.api.ClientResponse;
import ru.bsc.test.at.executor.helper.client.impl.http.HttpClient;
import ru.bsc.test.at.executor.model.Project;
import ru.bsc.test.at.executor.model.RequestData;
import ru.bsc.test.at.executor.model.Step;
import ru.bsc.test.at.executor.model.StepResult;

import java.util.Map;

/**
 * @author Pavel Golovkin
 */
@Slf4j
public class RestSimpleStepRequester extends RestAbstractStepRequester {

    public RestSimpleStepRequester(StepResult stepResult, Step step, String requestUrl, Object requestBody, Map requestHeaders, String testId, Project project, HttpClient httpClient, Map<String, Object> scenarioVariables, String projectPath) {
        super(stepResult, step, requestUrl, requestBody, requestHeaders, testId, project, httpClient, scenarioVariables, projectPath);
    }

    @Override
    public ClientResponse call() throws Exception {
        ClientResponse responseData;

        RequestData requestData = new RequestData();
        stepResult.getRequestDataList().add(requestData);

        responseData = getClientResponse(requestData);
        stepResult.setPollingRetryCount(1);
        return responseData;
    }
}
