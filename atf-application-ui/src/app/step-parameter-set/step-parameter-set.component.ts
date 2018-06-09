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

import {Component, Input, OnInit} from '@angular/core';
import {StepParameterSet} from '../model/step-parameter-set';
import {StepParameter} from '../model/step-parameter';
import {ToastOptions, ToastyService} from 'ng2-toasty';

@Component({
  selector: 'app-step-parameter-set',
  templateUrl: './step-parameter-set.component.html',
  styles: [
    '.no-border { border: 0; box-shadow: none; }'
  ]
})
export class StepParameterSetComponent implements OnInit {

  @Input()
  stepParameterSetList: StepParameterSet[];

  parameterNameList: string[];
  toastOptions: ToastOptions = {
    title: 'Warning',
    msg: 'Parameter name already exists',
    showClose: true,
    timeout: 15000,
    theme: 'bootstrap'
  };

  constructor(
    private toastyService: ToastyService
  ) { }

  ngOnInit() {
    this.parameterNameList = [];
    this.stepParameterSetList
      .forEach(parameterSet => parameterSet.stepParameterList
        .filter(parameter => !this.parameterNameList.find(value => value === parameter.name) && parameter.name != null)
        .forEach(parameter => this.parameterNameList.push(parameter.name)));
    this.parameterNameList = this.parameterNameList.sort((a, b) => a < b ? -1 : (a > b ? 1 : 0))
  }

  findParameter(stepParameterSet: StepParameterSet, parameterName: string): StepParameter {
    if (!stepParameterSet.stepParameterList) {
      stepParameterSet.stepParameterList = [];
    }
    const filtered = stepParameterSet.stepParameterList.filter(parameter => parameter.name === parameterName);
    return filtered.length > 0 ? filtered[0] : null;
  }

  addParameterSet(): void {
    const newStepParameterSet = new StepParameterSet();
    newStepParameterSet.stepParameterList = this.parameterNameList.map(parameterName => new StepParameter(parameterName));
    this.stepParameterSetList.push(newStepParameterSet);
  }

  addParameterName(): void {
    let newName: string;
    if (newName = prompt('New parameter name')) {
      if (this.isParameterNameExists(newName)) {
        this.toastyService.warning(this.toastOptions);
      } else {
        this.stepParameterSetList.forEach(value => value.stepParameterList.push(new StepParameter(newName)));
        this.parameterNameList.push(newName);
        this.parameterNameList = this.parameterNameList.sort((a, b) => a < b ? -1 : (a > b ? 1 : 0))
      }
    }
  }

  updateParameterName(oldParameterName: string): boolean {
    let newName: string;
    if (newName = prompt('New parameter name', oldParameterName)) {
      if (this.isParameterNameExists(newName)) {
        this.toastyService.warning(this.toastOptions);
      } else {
        this.stepParameterSetList
          .forEach(parameterSet => parameterSet.stepParameterList
            .filter(parameter => parameter.name === oldParameterName)
            .forEach(value => value.name = newName));
        this.parameterNameList = this.parameterNameList
          .map(parameterName => parameterName === oldParameterName ? newName : parameterName);
      }
    }
    return false;
  }

  isParameterNameExists(newName: string): boolean {
    return this.stepParameterSetList
      .find(parameterSet => parameterSet.stepParameterList
        .find(parameter => parameter.name === newName) != null) != null;
  }

  removeParameterSet(stepParameterSet: StepParameterSet): void {
    const indexToRemove = this.stepParameterSetList.indexOf(stepParameterSet);
    if (indexToRemove > -1) {
      this.stepParameterSetList.splice(indexToRemove, 1);
    }
  }
}
