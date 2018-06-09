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
import {Mapping} from '../../model/mapping';
import {WireMockService} from '../../service/wire-mock.service';

@Component({
  selector: 'app-mapping-list',
  templateUrl: './mapping-list.component.html'
})
export class MappingListComponent implements OnInit {

  mappingList: Mapping[];
  displayDetails = false;

  constructor(
    public wireMockService: WireMockService
  ) { }

  ngOnInit() {
    this.wireMockService.getMappingList()
      .then(mappingList => this.mappingList = mappingList
        .sort((a, b) => (
            a.request.url ? a.request.url : a.request.urlPattern) > (b.request.url ? b.request.url : b.request.urlPattern) ? 1 : -1
        ));
  }

  detailsToggle() {
    this.displayDetails = !this.displayDetails;
  }
}
