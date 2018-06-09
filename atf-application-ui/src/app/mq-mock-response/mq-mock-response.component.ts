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
import {MqMock} from '../model/mq-mock';
import {MqMockResponse} from "../model/mq-mock-response";

@Component({
  selector: 'app-mq-mock-response',
  templateUrl: './mq-mock-response.component.html'
})
export class MqMockResponseComponent implements OnInit{
  @Input()
  mqMockResponse: MqMock;

  @Output()
  onDelete = new EventEmitter<any>();

  severalResponses: boolean;

  ngOnInit() {
    this.severalResponses = this.mqMockResponse.responses.length > 1;
  }

  deleteMqMockResponse() {
    this.onDelete.emit();
  }

  addDestinationQueue() {
    this.mqMockResponse.responses.push(new MqMockResponse());
  }

  deleteDestinationQueue(index) {
    this.mqMockResponse.responses.splice(index, 1);
  }

  onToggleSeveralResponses() {
    if (!this.severalResponses) {
      this.mqMockResponse.responses.splice(1, this.mqMockResponse.responses.length - 1);
    }
  }
}
