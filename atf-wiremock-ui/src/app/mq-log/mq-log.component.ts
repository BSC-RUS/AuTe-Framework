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
import {MqLogItem} from '../../model/mq-log-item';
import {MqMockerService} from '../../service/mq-mocker-service';

@Component({
  selector: 'app-mq-log',
  templateUrl: './mq-log.component.html',
  styles: [
    '.clear-line { height: 30px; border-top-color: white; cursor: default; }'
  ]
})
export class MqLogComponent implements OnInit {

  mqLog: MqLogItem[] = [];
  selectedRequest: MqLogItem = null;
  tab = 'summary';
  sourceTextLimit = 1000;

  requestLimit = 30;

  constructor(
    public mqMockerService: MqMockerService
  ) { }

  ngOnInit() {
    this.updateList();
  }

  updateList() {
    this.sourceTextLimit = 1000;
    this.selectedRequest = null;
    this.mqMockerService.getRequestList(this.requestLimit)
      .subscribe(value => {
        this.mqLog = value;
      });
  }

  clear() {
    this.mqMockerService
      .clear()
      .subscribe(() => {
        this.updateList();
      });
  }

  select(mqLogItem: MqLogItem) {
    this.selectedRequest = mqLogItem;
    this.sourceTextLimit = 1000;
  }
}
