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

import {Component, ElementRef, HostListener, Input, OnInit, ViewChild} from '@angular/core';
import {FormControl} from '@angular/forms';
import	'rxjs/Rx';
import {Scenario} from '../model/scenario';
import {SearchScenarioService} from '../service/search-scenario.service';

@Component({
  selector: 'app-search',
  templateUrl: './search-scenario.component.html',
  styleUrls: ['./search-scenario.component.css']
})
export class SearchComponent implements OnInit {
  errorFlag: boolean;
  scenarioList: Scenario[];
  queryField: FormControl;
  @Input() projectCode;
  @ViewChild('searchResult') searchResult: ElementRef;
  @ViewChild('queryInput') queryInput: ElementRef;

  constructor(
    private searchScenarioService: SearchScenarioService
  ) { }

  ngOnInit() {
    this.queryField = new FormControl();
    this.queryField.valueChanges
      .debounceTime(400)
      .distinctUntilChanged()
      .subscribe( query => {
          if (query === '') {
            this.hiddenResultBlock();
            return;
          }
          this.search(query);
        }
      )
  }

  search(query) {
    this.errorFlag = false;
    this.searchScenarioService.searchByMethod(this.projectCode, query)
      .subscribe(
        (result) => {
                            if (!result.length) {
                                this.hiddenResultBlock(true);
                                return;
                            }
                            this.scenarioList = result;
                          },
        () => {
          this.hiddenResultBlock(true);
        }
      );
  }

  hiddenResultBlock(stateErrorFlag: boolean = false) {
    this.errorFlag = stateErrorFlag;
    this.scenarioList = null;
  }

  @HostListener('window:mousedown', ['$event.target'])
  onMouseDown(target) {
    const isShowSearchResult = (!this.searchResult || this.searchResult.nativeElement.contains(target))
                                || this.queryInput.nativeElement.contains(target);

    if (isShowSearchResult) {
      return;
    }

    this.hiddenResultBlock();
  }
}
