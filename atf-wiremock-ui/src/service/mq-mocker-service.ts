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

import {Http} from '@angular/http';
import {Injectable} from '@angular/core';
import {Observable} from 'rxjs/Observable';
import {MqLogItem} from '../model/mq-log-item';

@Injectable()
export class MqMockerService {

  public mqMockerAdminUrl = '/mq-mock/__admin';

  constructor(private http: Http) { }

  getRequestList(limit): Observable<MqLogItem[]> {
    return this.http
      .get(this.mqMockerAdminUrl + '/request-list' + (limit ? '?limit=' + limit : ''))
      .map(value => value.json() as MqLogItem[])
      .map(value => value.reverse());
  }

  clear(): Observable<any> {
    return this.http
      .post(this.mqMockerAdminUrl + '/request-list/clear', {});
  }
}
