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
import {Step} from '../model/step';
import 'rxjs/add/operator/map';
import {Observable} from 'rxjs/Observable';
import {Globals} from '../globals';

@Injectable()
export class StepService {

  public serviceUrl = '/rest/projects';
  private headers = new Headers({'Content-Type': 'application/json'});

  static isEquals(a, b): Boolean {
    if (a === b) {
      return true;
    }
    let valA = a === undefined ? null : ( a === false ? null : a);
    let valB = b === undefined ? null : ( b === false ? null : b);

    if (a instanceof Array && a.length === 0) {
      valA = null;
    }

    if (b instanceof Array && b.length === 0) {
      valB = null;
    }
    return valA === valB;
  }

  static differ(a, b, namespace?) {
    namespace = (namespace || '') + '.';

    if (StepService.isEquals(a, b)) {
      return [];
    }

    if (!a || !b) {
      return [{type: 'DELETED', id: namespace}];
    }

    const keysInA = Object.keys(a),
      keysInB = Object.keys(b);

    const diffA = keysInA.reduce(function(changes, key) {
      const ns = namespace + key;

      if (typeof b[key] === 'undefined') {
        return changes.concat([{type: 'DELETED', id: ns}]);
      }

      if (a[key] instanceof Array && b[key] instanceof Array) {
        if (a[key].length !== b[key].length) {
          return changes.concat([{type: 'CHANGED', id: ns}]);
        } else {
          for (let i = 0; i < a[key].length; i++) {
            const p = [];
            if (!StepService.isEquals(a[key][i], b[key][i])) {
              const c = StepService.differ(a[key], b[key], ns);
              if (c.length > 0) {
                p.push(c);
              }
            }
            if (p.length > 0) {
              return changes.concat(p);
            }
          }
        }
      } else if (a[key] !== null && typeof b[key] && typeof a[key] === 'object' && typeof b[key] === 'object') {
        return changes.concat(StepService.differ(a[key], b[key], ns));
      } else if (! StepService.isEquals(a[key], b[key])) {
        return changes.concat([{type: 'CHANGED', id: ns}]);
      }
      return changes;
    }, []);

    const diffB = keysInB.reduce(function(changes, key) {
      const ns = namespace + key;

      if (typeof a[key] === 'undefined') {
        return changes.concat([{ type: 'ADDED', id: ns }]);
      }

      return changes;
    }, []);

    return diffA.concat(diffB);
  }

  constructor(
    private globals: Globals,
    private http: Http
  ) { }

  saveStep(projectCode: string, scenarioGroup: string, scenarioCode: string, step: Step): Observable<Step> {
    const scenarioPath = (scenarioGroup ? scenarioGroup + '/' : '') + scenarioCode;
    return this.http.put(
      this.globals.serviceBaseUrl + this.serviceUrl + '/' + projectCode + '/scenarios/' + scenarioPath + '/steps/' + step.code,
      step,
      {headers: this.headers}
    ).map(value => value.json() as Step);
  }

  cloneStep(projectCode: string, scenarioGroup: string, scenarioCode: string, step: Step): Observable<Step> {
    const scenarioPath = (scenarioGroup ? scenarioGroup + '/' : '') + scenarioCode;
    return this.http.post(
      this.globals.serviceBaseUrl + this.serviceUrl + '/' + projectCode + '/scenarios/' + scenarioPath + '/steps/' + step.code + '/clone',
      {},
      {headers: this.headers}
    ).map(value => value.json() as Step);
  }

  copyStep(step: Step): Step {
    return Object.assign({}, step);
  }

  equals(s1: Step, s2: Step): boolean {
     return JSON.stringify(s1, this.replacer) === JSON.stringify(s2, this.replacer);
  }

  replacer = function (name, val) {
    if (name === 'stepMode' && !val) {
      return 'REST';
    }
    return val;
  };

}
