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
import * as JsDiff from 'diff';

@Component({
  selector: 'app-diff',
  templateUrl: './diff.component.html',
  styleUrls: ['./diff.component.scss']
})
export class DiffComponent implements OnInit {
  @Input('expected') expected: string;
  @Input('actual') actual: string;
  expectedDiff: any[];
  actualDiff: any[];
  syncScroll = true;

  private static prepareStringForComparison(str: string): string {
    if (!str) {
      return '';
    }
    const resultObject: any = DiffComponent.tryToParseAsJSON(str);
    return resultObject ?
      JSON.stringify(resultObject, null, 2) :
      str.replace(/\r/g, '').replace(/\t/g, '  ').trim();
  }

  private static tryToParseAsJSON(str: string): any {
    try {
      return JSON.parse(str);
    } catch (e) {
      return null;
    }
  }

  ngOnInit() {
    this.formDiff();
  }

  private formDiff(): void {
    const actualResultStr: string = DiffComponent.prepareStringForComparison(this.actual);
    const expectedResultStr: string = DiffComponent.prepareStringForComparison(this.expected);

    const diff: any[] = JsDiff.diffLines(expectedResultStr, actualResultStr);

    diff.forEach((item, i, arr) => {
      if (item.removed && arr[i + 1] && arr[i + 1].added) {
        item.rowDiff = arr[i + 1].rowDiff = JsDiff.diffWordsWithSpace(item.value, arr[i + 1].value);
      }
    });

    this.expectedDiff = diff.filter(item => !item.added).map(item => {
      if (item.rowDiff) {
        item.rowDiff = item.rowDiff.filter(rowDiffItem => !rowDiffItem.added);
      }
      return item;
    });

    this.actualDiff = diff.filter(item => !item.removed).map(item => {
      if (item.rowDiff) {
        item.rowDiff = item.rowDiff.filter(rowDiffItem => !rowDiffItem.removed);
      }
      return item;
    });
  }
}
