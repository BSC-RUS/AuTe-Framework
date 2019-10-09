/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

import {Injectable} from '@angular/core';
import {Headers, Http} from '@angular/http';

import 'rxjs/add/operator/toPromise';
import {Mapping} from '../model/mapping';
import {Observable} from 'rxjs/Observable';
import {RequestList} from '../model/request-list';
import 'rxjs/add/operator/map';
import {JmsMapping, JmsMappings} from "../model/jms-mapping";

@Injectable()
export class WireMockService {

  // URL to WireMock
  public adminUrl = '/__admin';
  public jmsUrl = '/mq-mock' + this.adminUrl;
  private headers = new Headers({'Content-Type': 'text/plain'});

  constructor(private http: Http) { }

  getMappingList(): Promise<Mapping[]> {
    return this.http
      .get(this.adminUrl + '/mappings')
      .toPromise()
      .then(response => response.json().mappings as Mapping[])
      .catch(reason => console.log(reason));
  }

  getJmsMappings(): Promise<JmsMappings> {
    return this.http
      .get(this.jmsUrl + '/mappings/group')
      .toPromise()
      .then(response => response.json() as JmsMappings)
      .catch(reason => console.log(reason));
  }

  deleteOne(mapping: Mapping): Promise<null> {
    return this.http
      .delete(this.adminUrl + '/mappings/' + mapping.uuid).toPromise();
  }

  deleteJmsMapping(mapping: JmsMapping): Promise<null> {
    return this.http.delete(this.jmsUrl + '/mappings/' + mapping.guid).toPromise();
  }

  apply(mapping: Mapping): Promise<Mapping> {
    if (mapping.uuid) {
      return this.http
        .put(
          this.adminUrl + '/mappings/' + mapping.uuid,
          mapping,
          {headers: this.headers}
        )
        .toPromise()
        .then(response => response.json() as Mapping)
        .catch(reason => console.log(reason));
    } else {
      return this.http
        .post(
          this.adminUrl + '/mappings',
          mapping,
          {headers: this.headers}
        )
        .toPromise()
        .then(response => response.json() as Mapping)
        .catch(reason => console.log(reason));
    }
  }

  applyJms(mapping: JmsMapping): Promise<string> {
    return mapping.guid ?
      this.http.post(this.jmsUrl + '/mappings/' + mapping.guid, mapping)
        .toPromise()
        .then(() => mapping.guid) :
      this.http.post(this.jmsUrl + '/add-mapping', mapping)
        .toPromise()
        .then(response => response.text());
  }

  saveToBackStorage(): Promise<null> {
    return this.http
      .post(
        this.adminUrl + '/mappings/save',
        null,
        {headers: this.headers}
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

  findJmsMapping(guid: string): Promise<JmsMapping> {
    return this.http
      .get(this.jmsUrl + '/mappings/' + guid)
      .toPromise()
      .then(response => response.json() as JmsMapping)
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
        {headers: this.headers}
      );
  }
}
