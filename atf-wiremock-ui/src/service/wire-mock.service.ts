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

import { Injectable } from '@angular/core';
import {Headers, Http} from '@angular/http';

import 'rxjs/add/operator/toPromise';
import {Mapping} from '../model/mapping';
import {Observable} from 'rxjs/Observable';
import {RequestList} from '../model/request-list';
import 'rxjs/add/operator/map';

@Injectable()
export class WireMockService {

  // URL to WireMock
  public adminUrl = '/__admin';
  private headers = new Headers({'Content-Type': 'text/plain'});

  constructor(private http: Http) { }

  getMappingList(): Promise<Mapping[]> {
    return this.http
      .get(this.adminUrl + '/mappings')
      .toPromise()
      .then(response => response.json().mappings as Mapping[])
      .catch(reason => console.log(reason));
  }

  deleteOne(mapping: Mapping) {
    this.http
      .delete(this.adminUrl + '/mappings/' + mapping.uuid);
  }

  apply(mapping: Mapping): Promise<Mapping> {
    if (mapping.uuid) {
      return this.http
        .put(
          this.adminUrl + '/mappings/' + mapping.uuid,
          mapping,
          { headers: this.headers }
        )
        .toPromise()
        .then(response => response.json() as Mapping)
        .catch(reason => console.log(reason));
    } else {
      return this.http
        .post(
          this.adminUrl + '/mappings',
          mapping,
          { headers: this.headers }
        )
        .toPromise()
        .then(response => response.json() as Mapping)
        .catch(reason => console.log(reason));
    }
  }

  saveToBackStorage(): Promise<null> {
    return this.http
      .post(
        this.adminUrl + '/mappings/save',
        null,
        { headers: this.headers }
      )
      .toPromise()
      .catch(reason => console.log(reason));
  }

  findOne(uuid: string): Promise<Mapping> {
    return this.http
      .get(this.adminUrl + '/mappings/' + uuid)
      .toPromise()
      .then(response => response.json() as Mapping)
      .catch(reason => console.log(reason));
  }

  getRequestList(limit?): Observable<RequestList> {
    return this.http
      .get(limit ? this.adminUrl + '/requests?limit=' + (limit ? limit : 50) : this.adminUrl + '/requests')
      .map(value => value.json() as RequestList);
  }

  clearRequestList(): Observable<any> {
    return this.http
      .post(
        this.adminUrl + '/requests/reset',
        null,
        { headers: this.headers }
      );
  }
}
