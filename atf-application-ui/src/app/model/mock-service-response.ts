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

export class MockServiceResponse {
  code: string;
  serviceUrl: string;
  urlPattern: boolean;
  httpMethod: string;
  responseBody: string;
  headers: HeaderItem[];
  httpStatus: number;
  contentType: number;
  userName: string;
  password: string;
  pathFilter: string;
}

export class HeaderItem {
  headerName: string;
  headerValue: string;
  compareType: string = 'equalTo';
}
