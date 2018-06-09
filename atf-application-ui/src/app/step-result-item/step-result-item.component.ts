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

import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {StepResult} from '../model/step-result';
import {StepService} from '../service/step.service';
import {CustomToastyService} from '../service/custom-toasty.service';
import {ActivatedRoute, ParamMap} from '@angular/router';
import {Scenario} from '../model/scenario';
import {Step} from '../model/step';
import {StepItemComponent} from '../step-item/step-item.component';

@Component({
  selector: 'app-step-result-item',
  templateUrl: './step-result-item.component.html',
  styles: [
    '.nav-tabs > li > a { padding-top: 3px; padding-bottom: 3px; }',
    '.tab-content { border: 1px solid #ddd; border-top-width: 0;}',
    '.row { margin-bottom: 5px; }',
    '.input-group-btn > select { padding: 0; width: 85px; border-top-left-radius: 5px; border-bottom-left-radius: 5px; border-right: 0; }'
  ]
})
export class StepResultItemComponent implements OnInit {

  @Input()
  stepResult: StepResult;
  @Input()
  scenario: Scenario;
  @Input()
  stepList: Step[];

  @ViewChild(StepItemComponent) stepItem: StepItemComponent;

  step: Step;
  expectedDiff: string;
  actualDiff: string;

  readonly = true;
  showRestartNotify = false;

  tab = 'details';
  projectCode: string;
  displayDetails = false;
  changed: Boolean = false;

  /** оборачиваем строку */
  static wrapChanged(text: string, classToWrap: string) {
    let wrapped: string;
    if (text.endsWith('\n')) {
      wrapped = '<span class="' + classToWrap + '">' + text.substr(0, text.length - 1) + '</span>' + '\n';
    } else {
      wrapped = '<span class="' + classToWrap + '">' + text + '</span>';
    }
    return wrapped;
  }

  /** оборачиваем строку */
  static wrapToDiv(span: String) {
    return '<div class="unchanged-row">' + span + '</div>';
  }

  constructor(
    private route: ActivatedRoute,
    private stepService: StepService,
    private customToastyService: CustomToastyService
  ) {}

  ngOnInit() {
    this.route.params.subscribe((params: ParamMap) => {
      this.projectCode = params['projectCode'];
      this.formatText();
    });

    this.step = this.stepResult.step;
    if (this.stepList) {
      this.showRestartNotify = true;
      const foundStep = this.stepList.find(s => s.code === this.step.code);
      const diffs = StepService.differ(foundStep, this.step);
      console.log('diffs = ', diffs);
      if (diffs.length === 0) {
        this.step = foundStep;
        this.readonly = false;
      }
    }
  }

  formatText() {

    const diffs: any = this.stepResult.diff;
    if (!diffs) {
      return;
    }

    this.expectedDiff = '';
    this.actualDiff = '';

    // выделяем запись в ожидаемых, в случае если прерации EQUAL-INSERT-EQUAL и rownum записей не отличаются
    const expectedChanges: number[] = [];
    let prevOps = '';
    let prevText = '';
    let rowNum = 0;
    let prevRowNum = 0;
    let insertText = '';

    const pattern_amp = /&/g;
    const pattern_lt = /</g;
    const pattern_gt = />/g;


    for (let x = 0; x < diffs.length; x++) {
      const op = diffs[x].operation;    // Operation (insert, delete, equal)
      let text = diffs[x].text;
      text = text.replace(pattern_amp, '&amp;').replace(pattern_lt, '&lt;').replace(pattern_gt, '&gt;');
      switch (op) {
        case 'INSERT':
        {
          this.actualDiff = this.actualDiff.concat(StepResultItemComponent.wrapChanged(text, 'added'));
          prevOps = prevOps + 'I';
          insertText = text;
          break;
        }
        case 'DELETE':
        {
          rowNum = rowNum + text.split('\n').length - 1;
          this.expectedDiff = this.expectedDiff.concat(StepResultItemComponent.wrapChanged(text, 'removed'));
          prevOps = '';
          break;
        }
        case 'EQUAL':
        {
          const splitted = text.split('\n');
          rowNum = rowNum + splitted.length - 1;
          this.actualDiff = this.actualDiff.concat(text);
          this.expectedDiff = this.expectedDiff.concat(text);
          const iLength = insertText.split('\n').filter(v => v.trim() !== '').length;

          // последняя строка предпоследнего изменения
          const lastPrev = prevText.split('\n').pop().trim();
          const firstCurrent = text.split('\n')[0];

          // нам надо выделить строку
          if (prevOps === 'EI' && iLength > 1 && !this.actualDiff.includes(lastPrev + firstCurrent)) {
            expectedChanges.push(prevRowNum);
          }
          insertText = '';
          prevOps = 'E';
          prevText = text;
          prevRowNum = rowNum;
          break;
        }
      }
    }

    this.actualDiff = this.wrapChangedLines(this.actualDiff, 'added', 'added-row', []);
    this.expectedDiff = this.wrapChangedLines(this.expectedDiff, 'removed', 'removed-row', expectedChanges);

  }

  /** оборачиваем строку, если в ней есть изменения */
  wrapChangedLines(text: string, classToWrap: string, classWrap: string, expectedChanges: number[]) {
    const beginPattern = '<span class="' + classToWrap + '">';
    const endPattern = '</span>';

    const lines = text.split('\n');
    const results = [];
    let resultsToDiv = [];

    const hasChanges = function (line: string): boolean {
      return line.indexOf(beginPattern) !== -1 && line.indexOf(beginPattern) !== -1;
    };

    const hasUnclosedStart = function (line: string): boolean {
      let index = 0;
      let closed = false;
      let beginPatternIndex = line.indexOf(beginPattern, index);
      let endPatternIndex = line.indexOf(endPattern, index);
      while (beginPatternIndex !== -1 || endPatternIndex !== -1) {
        if (beginPatternIndex !== -1) {
          closed = false;
          index = beginPatternIndex + beginPattern.length;
        }

        if (endPatternIndex === -1 && !closed) {
          closed = true;
        } else if (endPatternIndex !== -1) {
          index = endPatternIndex + endPattern.length;
        }
        beginPatternIndex = line.indexOf(beginPattern, index);
        endPatternIndex = line.indexOf(endPattern, index);
      }
      return closed;
    };


    const hasClosedEnd = function (line: string): boolean {
      let index = 0;
      let closed = false;
      let beginPatternIndex = line.indexOf(beginPattern, index);
      let endPatternIndex = line.indexOf(endPattern, index);
      while (beginPatternIndex !== -1 || endPatternIndex !== -1) {

        if (beginPatternIndex !== -1) {
          closed = false;
          index = beginPatternIndex + beginPattern.length;
        }
        endPatternIndex = line.indexOf(endPattern, index);
        if (endPatternIndex !== -1) {
          closed = true;
          index = endPatternIndex + endPattern.length;
        }
        beginPatternIndex = line.indexOf(beginPattern, index);
      }
      return closed;
    };


    let wasChanges = false;
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i];

      // появилась строка с изменениями, но до этого были строки без изменений
      const changed = hasChanges(line);
      const unclosedStart = hasUnclosedStart(line);
      const closedEnd = hasClosedEnd(line);
      const oneStringDiff = changed && !unclosedStart && !closedEnd;

      if (changed && !wasChanges) {
        if (resultsToDiv.length > 0) {
          results.push(StepResultItemComponent.wrapToDiv(StepResultItemComponent.wrapChanged(resultsToDiv.join('\n'), 'diff-content')));
          resultsToDiv = [];
        }
        wasChanges = false;
      }
      if (unclosedStart) {
        wasChanges = true;
      }

      if (oneStringDiff) {
        if (resultsToDiv.length > 0) {
          results.push(
            StepResultItemComponent.wrapToDiv(
              StepResultItemComponent.wrapChanged(resultsToDiv.join('\n'), wasChanges ? classWrap : 'diff-content')
            )
          );
          resultsToDiv = [];
        }
        wasChanges = true;
      }

      //noinspection TypeScriptValidateTypes
      if (!changed && expectedChanges.find(x => x === i)) {
        resultsToDiv.push(StepResultItemComponent.wrapChanged(line, 'removed-row'));
      } else {
        resultsToDiv.push(line);
      }


      if (oneStringDiff || closedEnd) {
        if (resultsToDiv.length > 0) {
          results.push(StepResultItemComponent.wrapToDiv(StepResultItemComponent.wrapChanged(resultsToDiv.join('\n'), classWrap)));
          resultsToDiv = [];
        }
        wasChanges = false;
      }
    }

    if (resultsToDiv.length > 0) {
      results.push(
        StepResultItemComponent.wrapToDiv(
          StepResultItemComponent.wrapChanged(resultsToDiv.join('\n'), wasChanges ? classWrap : 'diff-content')
        )
      );
    }

    return results.join('\n');
  }

  selectTab(tabName: string) {
    this.tab = tabName;
    return false;
  }

  saveStep() {
    const toasty = this.customToastyService.saving();
    this.stepService.saveStep(this.projectCode, this.scenario.scenarioGroup, this.scenario.code, this.step)
      .subscribe(() => {
        this.refreshStepList();
        this.stepItem.resetChangeState();
        setTimeout(() => {
          this.changed = false
        });
         this.customToastyService.success('Сохранено', 'Шаг сохранен');
      }, error => this.customToastyService.error('Ошибка', error), () => this.customToastyService.clear(toasty));
  }

  refreshStepList() {
    if (this.stepList) {
      const index = this.stepList.findIndex(s => s.code === this.step.code);
      if (index >= 0) {
        this.stepList[index] = this.stepService.copyStep(this.step);
      }
    }
  }
}
