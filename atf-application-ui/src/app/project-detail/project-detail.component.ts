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

import {AfterContentChecked, Component, OnInit, QueryList, ViewChildren} from '@angular/core';
import {ProjectService} from '../service/project.service';
import {Project} from '../model/project';
import {ActivatedRoute, ParamMap, Router} from '@angular/router';
import 'rxjs/add/operator/switchMap';
import {Scenario} from '../model/scenario';
import {ScenarioListItemComponent} from '../scenario-list-item/scenario-list-item.component';
import { saveAs } from 'file-saver/FileSaver';
import {CustomToastyService} from '../service/custom-toasty.service';
import {ScenarioService} from '../service/scenario.service';
import {TranslateService} from '@ngx-translate/core';
import {ScenarioIdentity} from '../model/scenario-identity';

@Component({
  selector: 'app-project-detail',
  templateUrl: './project-detail.component.html',
  styleUrls: ['project-detail.component.css']
})
export class ProjectDetailComponent implements OnInit, AfterContentChecked {

  project: Project;
  scenarioList: Scenario[];
  filter: Scenario;
  selectAllFlag = false;
  failCount = 0;
  Math: any;
  newScenarioName = '';
  scenarioGroupList: string[] = [];

  @ViewChildren(ScenarioListItemComponent) scenarioComponentList: QueryList<ScenarioListItemComponent>;
  executingStateExecuted = 0;
  executingStateTotal = 0;

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private projectService: ProjectService,
    private customToastyService: CustomToastyService,
    private scenarioService: ScenarioService,
    private translate: TranslateService
  ) {
    this.Math = Math;
  }

  ngOnInit() {
    this.route.paramMap
      .switchMap((params: ParamMap) => this.projectService.findOne(params.get('projectCode')))
      .subscribe(value => {
        this.project = value;

        this.route
          .queryParams
          .subscribe(params => {
            if (params['scenarioGroup']) {
              this.filter = new Scenario();
              this.filter.scenarioGroup = params['scenarioGroup'];
            }
          });
      });

    this.route.paramMap
      .switchMap((params: ParamMap) => this.projectService.findScenariosByProject(params.get('projectCode')))
      .subscribe(value => {
        this.scenarioList = value;
        this.scenarioList
          .map(scenario => scenario.scenarioGroup)
          .forEach(groupName => {
            if (groupName && this.scenarioGroupList.indexOf(groupName) === -1) {
              this.scenarioGroupList.push(groupName);
            }
          });
      });
  }

  ngAfterContentChecked(): void {
    this.updateFailCountSum();
  }

  selectGroup(group?: string): boolean {
    this.filter = new Scenario();
    this.filter.scenarioGroup = group;

    this.router.navigate([], {queryParams: {scenarioGroup: group ? group : ''}});
    this.updateFailCountSum();

    this.updateSelectAllFlag();
    return false;
  }

  selectAllGroups(): boolean {
    this.filter = null;

    this.router.navigate([]);
    this.updateFailCountSum();

    this.updateSelectAllFlag();
    return false;
  }

  updateSelectAllFlag() {
    if (this.scenarioList) {
      this.selectAllFlag = this.scenarioList
        .filter(s => !this.filter || s.scenarioGroup == this.filter.scenarioGroup)
        .filter(s => !s._selected).length === 0;
    } else {
      this.selectAllFlag = false;
    }
  }

  isDisplayScenario(scenario: Scenario) {
    return !this.filter ||
      (!this.filter.scenarioGroup && !scenario.scenarioGroup) ||
      (this.filter.scenarioGroup && scenario && scenario.scenarioGroup &&
          scenario.scenarioGroup === this.filter.scenarioGroup
      );
  }

  executeSelectedScenarios() {
    this.scenarioComponentList
      .filter(item => item.scenario._selected && this.isDisplayScenario(item.scenario))
      .forEach(value => value.runScenario());
  }

  selectAll() {
    this.selectAllFlag = !this.selectAllFlag;
    this.scenarioComponentList
      .filter(item => this.isDisplayScenario(item.scenario))
      .forEach(item => item.scenario._selected = this.selectAllFlag);
  }

  updateFailCountSum() {
    if (this.scenarioList) {
      this.failCount = this.scenarioList
        .filter(item => this.isDisplayScenario(item))
        .filter(value => value.failed)
        .length;
    } else {
      this.failCount = 0;
    }
    return this.failCount;
  }

  updateExecutionStatus() {
    this.executingStateExecuted = this.scenarioComponentList
      .filter(item => item.state === 'executing' || item.state === 'finished' || item.state === 'starting')
      .filter(item => item.executedSteps)
      .map(item => item.executedSteps - (item.state === 'finished' ? 0 : 1))
      .reduce((a, b) => a + b, 0);
    this.executingStateTotal = this.scenarioComponentList
      .filter(item => item.state === 'executing' || item.state === 'finished' || item.state === 'starting')
      .filter(item => item.totalSteps)
      .map(item => item.totalSteps)
      .reduce((a, b) => a + b, 0);
  }

  // noinspection JSUnusedLocalSymbols
  onStateChange(event: any, scenario: Scenario) {
    this.updateExecutionStatus();
  }

  // noinspection JSUnusedLocalSymbols
  onCbStateChange(event: any, scenario: Scenario) {
    this.selectAllFlag = !scenario._selected && this.scenarioList
      .filter(s => !this.filter || s.scenarioGroup == this.filter.scenarioGroup)
      .filter(s => s !== scenario && !s._selected).length === 0;
  }

  saveNewScenario() {
    const newScenario = new Scenario();
    newScenario.name = this.newScenarioName;
    if (this.filter && this.filter.scenarioGroup) {
      newScenario.scenarioGroup = this.filter.scenarioGroup;
    }

    const toasty = this.customToastyService.saving('Сохранение сценария...', 'Сохранение может занять некоторое время...');
    this.projectService.createScenario(this.project, newScenario)
      .subscribe(savedScenario => {
        this.scenarioList.push(savedScenario);
        this.newScenarioName = '';
        this.customToastyService.success('Сохранено', 'Сценарий создан');
      }, error => this.handleError(error), () => this.customToastyService.clear(toasty));
  }

  private handleError(error: any) {
    const message = JSON.parse(error._body).message;
    console.log(message);
    this.translate.get(message).subscribe(value => {
      this.customToastyService.error('Ошибка', value);
    });
  }

  getReportsBySelectedScenarios() {
    const scenarioIdentities = [];
    this.scenarioComponentList
      .filter(item => item.scenario._selected && this.isDisplayScenario(item.scenario) && item.scenario.hasResults)
      .forEach(item => {
        const identity = new ScenarioIdentity(item.projectCode, item.scenario.scenarioGroup, item.scenario.code);
        scenarioIdentities.push(identity);
      });

    this.scenarioService
      .downloadReport(scenarioIdentities)
      .subscribe(blobReport => {
        saveAs(blobReport, 'reports.zip')
      });
  }
}
