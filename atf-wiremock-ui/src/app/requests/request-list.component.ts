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

import { Component, OnInit } from '@angular/core';
import {WireMockService} from '../../service/wire-mock.service';
import {RequestList} from '../../model/request-list';
import {Request} from '../../model/request';

@Component({
  selector: 'app-requests',
  templateUrl: './request-list.component.html',
  styles: [
    '.tab-content { border: 1px solid #ddd; border-top-width: 0;}'
  ]
})
export class RequestListComponent implements OnInit {

  requestList: RequestList;
  selectedRequest: Request;
  tab = 'summary';
  requestLimit = 30;

  constructor(
    public wireMockService: WireMockService
  ) { }

  ngOnInit() {
    this.updateRequestList();
  }

  updateRequestList() {
    this.selectedRequest = null;
    this.requestList = null;
    this.wireMockService
      .getRequestList(this.requestLimit)
      .subscribe(value => this.requestList = value);
  }

  clearRequestList() {
    if (confirm('Confirm: clear request list')) {
      this.selectedRequest = null;
      this.requestList = null;
      this.wireMockService.clearRequestList().subscribe(() => this.updateRequestList());
    }
  }

  select(request: Request) {
    this.selectedRequest = request;
    return false;
  }

  selectTab(newTab: string) {
    this.tab = newTab;
    return false;
  }
}
