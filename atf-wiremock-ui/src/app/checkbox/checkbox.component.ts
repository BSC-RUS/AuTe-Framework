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

import {Component, EventEmitter, Input, Output} from '@angular/core';

export enum CheckboxStatus {
  FALSE = 0,
  TRUE = 1,
  INDETERMINATE = 2
}

@Component({
  selector: 'app-checkbox',
  templateUrl: './checkbox.component.html'
})
export class CheckboxComponent {

  @Input()
  selected: CheckboxStatus;
  @Output()
  toggle = new EventEmitter<CheckboxStatus>();

  private setStatus(v: any) {
    if (v.indeterminate) {
      this.selected = CheckboxStatus.INDETERMINATE;
    } else if (v.checked) {
      this.selected = CheckboxStatus.TRUE;
    } else {
      this.selected = CheckboxStatus.FALSE;
    }
  }

  onToggle(e: any) {
    this.setStatus(e.target);

    this.toggle.emit(this.selected);
  }
}
