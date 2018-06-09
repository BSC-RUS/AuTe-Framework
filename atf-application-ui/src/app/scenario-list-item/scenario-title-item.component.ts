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

import {Component, ElementRef, EventEmitter, HostListener, Output, QueryList, ViewChildren} from '@angular/core';
import {AfterViewInit} from '@angular/core/src/metadata/lifecycle_hooks';
import {ScenarioListItemComponent} from './scenario-list-item.component';

@Component({
  selector: 'app-scenario-title-item',
  templateUrl: './scenario-title-item.component.html',
  styleUrls: ['./scenario-list-item.component.css']
})
export class ScenarioTitleItemComponent extends ScenarioListItemComponent implements AfterViewInit {
  @ViewChildren('scenarioHeader') headerRefs: QueryList<ElementRef>;
  header: any;
  headerOffset: number;

  @Output() onSaveSteps = new EventEmitter<any>();

  ngAfterViewInit() {
    this.header = this.headerRefs.first.nativeElement;
    this.headerOffset = this.header.offsetTop;
  }

  @HostListener('window:scroll')
  onScrollHandler() {
    if (window.pageYOffset >= this.headerOffset) {
      this.header.classList.add('sticky');
    } else {
      this.header.classList.remove('sticky');
    }
  }

  resultDetailsToggle() {
    if (this.showResultDetails) {
      document.body.style.overflow = 'visible';
      this.header.classList.remove('full-page');
    } else {
      document.body.style.overflow = 'hidden';
      this.header.classList.add('full-page');
    }
    super.resultDetailsToggle();
  }

  saveSteps() {
    this.onSaveSteps.emit();
  }
}
