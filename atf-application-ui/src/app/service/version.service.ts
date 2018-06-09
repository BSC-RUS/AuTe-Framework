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

import {Injectable} from '@angular/core';
import {Http} from '@angular/http';
import {Globals} from '../globals';
import {Observable} from 'rxjs/Observable';
import {Version} from '../model/version';
import {WiremockVersion} from "../model/wiremock-version";

@Injectable()
export class VersionService {

  public applicationVersionUrl = '/rest/version/application';
  public wiremockVersionUrl = '/rest/version/wiremock';

  constructor(
    private globals: Globals,
    private http: Http
  ) { }

  getApplicationVersion(): Observable<Version> {
    return this.getVersion(this.applicationVersionUrl);
  }

  getProjectsWiremockVersions(): Observable<Array<WiremockVersion>> {
    return this.http.get(this.globals.serviceBaseUrl + this.wiremockVersionUrl)
      .map(value => value.json() as Array<WiremockVersion>);
  }

  getVersion(url): Observable<Version> {
    return this.http.get(this.globals.serviceBaseUrl + url).map(value => value.json() as Version);
  }
}
