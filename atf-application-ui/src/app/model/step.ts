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

import {ExpectedServiceRequest} from './expected-service-request';
import {MockServiceResponse} from './mock-service-response';
import {StepParameterSet} from './step-parameter-set';
import {FormData} from './form-data';
import {MqMock} from './mq-mock';
import {ExpectedMqRequest} from './expected-mq-request';
import {SqlData} from './sql-data';
import {ScenarioVariableFromMqRequest} from './scenario-variable-from-mq-request';
import {MqMessage} from "./mq-message";

export class Step {
  code: string;
  expectedServiceRequestList: ExpectedServiceRequest[];
  relativeUrl: string;
  requestMethod: string;
  request: string;
  requestHeaders: string;
  expectedResponse: string;
  expectedResponseIgnore: boolean;
  expectedStatusCode: number;
  jsonXPath: string;
  requestBodyType = 'JSON';
  usePolling: boolean;
  pollingJsonXPath: string;
  mockServiceResponseList: MockServiceResponse[];
  disabled: boolean;
  stepComment: string;
  savedValuesCheck: any = {};
  stepParameterSetList: StepParameterSet[] = [];
  responseCompareMode = 'JSON';
  formDataList: FormData[] = [];
  multipartFormData: boolean;
  mqMessages: MqMessage[];
  jsonCompareMode = 'NON_EXTENSIBLE';
  script: string;
  numberRepetitions: string;
  parseMockRequestUrl: string;
  parseMockRequestXPath: string;
  parseMockRequestScenarioVariable: string;
  timeoutMs: string;
  mqMockResponseList: MqMock[];
  expectedMqRequestList: ExpectedMqRequest[];
  sqlDataList: SqlData[];
  scenarioVariableFromMqRequestList: ScenarioVariableFromMqRequest[];
  stepMode: string;

  mqOutputQueueName: string;
  mqInputQueueName: string;
  mqTimeoutMs: string;

}
