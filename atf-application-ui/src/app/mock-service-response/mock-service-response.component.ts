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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {HeaderItem, MockServiceResponse} from '../model/mock-service-response';

@Component({
  selector: 'app-mock-service-response',
  templateUrl: './mock-service-response.component.html',
  styles: [
    '.nav-tabs > li > a { padding: 3px 7px; }',
    '.tab-content { border: 1px solid #ddd; border-top-width: 0;}'
  ]
})
export class MockServiceResponseComponent implements OnInit {

  @Input()
  mockServiceResponse: MockServiceResponse;

  @Output()
  onDelete = new EventEmitter<any>();

  tab = 'responseBody';

  constructor() { }

  ngOnInit() {
    if(!this.mockServiceResponse.headers){
      this.mockServiceResponse.headers = [];
    }
  }

  selectTab(tabName: string) {
    this.tab = tabName;
    return false;
  }

  addHeader() {
    this.mockServiceResponse.headers.push(new HeaderItem());
  }

  deleteHeader(header: HeaderItem){
    this.mockServiceResponse.headers = this.mockServiceResponse.headers.filter(value => value !== header);
  }

  deleteResponse() {
    this.onDelete.emit();
  }
}
