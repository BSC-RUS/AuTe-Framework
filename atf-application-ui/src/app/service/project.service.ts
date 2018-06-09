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
import {Observable} from 'rxjs/Observable';
import {Project} from '../model/project';
import {Headers, Http, Response} from '@angular/http';
import 'rxjs/add/operator/map';
import {Scenario} from '../model/scenario';
import {Globals} from '../globals';

@Injectable()
export class ProjectService {

  public serviceUrl = '/rest/projects';
  private headers = new Headers({'Content-Type': 'application/json'});

  constructor(
    private globals: Globals,
    private http: Http
  ) { }

  findAll(): Observable<Project[]> {
    return this.http.get(this.globals.serviceBaseUrl + this.serviceUrl)
      .map(value => value.json() as Project[]);
  }

  save(project: Project): Observable<Project> {
    return this.http.put(
      this.globals.serviceBaseUrl + this.serviceUrl + '/' + project.code,
      project,
      { headers: this.headers }
    ).map(value => value.json() as Project);
  }

  create(project: Project): Observable<Project> {
    return this.http.put(
      this.globals.serviceBaseUrl + this.serviceUrl,
      project,
      { headers: this.headers }
    ).map(value => value.json() as Project);
  }

  findOne(projectCode: string): Observable<Project> {
    return this.http.get(this.globals.serviceBaseUrl + this.serviceUrl + '/' + projectCode)
      .map(value => value.json() as Project);
  }

  findScenariosByProject(projectCode: string): Observable<Scenario[]> {
    return this.http.get(this.globals.serviceBaseUrl + this.serviceUrl + '/' + projectCode + '/scenarios')
      .map(value => value.json() as Scenario[]);
  }

  createScenario(project: Project, scenario: Scenario): Observable<Scenario> {
    return this.http.post(
      this.globals.serviceBaseUrl + this.serviceUrl + '/' + project.code + '/scenarios',
      scenario,
      {headers: this.headers}
    ).map(value => value.json() as Scenario);
  }

  addNewGroup(projectCode: string, groupName: string): Observable<string[]> {
    return this.http.post(
      this.globals.serviceBaseUrl + this.serviceUrl + '/' + projectCode + '/group',
      groupName,
      {headers: this.headers}
    ).map(value => value.json() as string[]);
  }

  renameGroup(projectCode: string, oldGroupName: string, newGroupName: string) {
    return this.http.put(
      this.globals.serviceBaseUrl + this.serviceUrl + '/' + projectCode + '/group',
      {
        oldGroupName: oldGroupName,
        newGroupName: newGroupName
      },
      {headers: this.headers}
    ).map(value => value.json() as string[]);
  }
}
