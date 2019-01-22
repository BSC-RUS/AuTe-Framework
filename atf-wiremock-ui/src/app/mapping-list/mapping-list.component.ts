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

import {Component, OnInit} from '@angular/core';
import {Mapping} from '../../model/mapping';
import {WireMockService} from '../../service/wire-mock.service';
import {EventService} from "../../service/event-service";

@Component({
  selector: 'app-mapping-list',
  templateUrl: './mapping-list.component.html'
})
export class MappingListComponent implements OnInit {

  mappingList: Mapping[];
  displayDetails = false;
  scenariosName = [];

  constructor(
    public wireMockService: WireMockService,
    private eventService: EventService
  ) { }

  ngOnInit() {
    this.getMappings();
    this.eventService.updateMappingList.subscribe(value => {
      if(value) {
        this.getMappings();
      }
    })
  }

  getMappings() {
    this.wireMockService.getMappingList()
      .then(mappingList => {
        this.mappingList = mappingList
          .sort((a, b) =>
            (a.name ? a.name : (a.request.url ? a.request.url : (a.request.urlPattern ? a.request.urlPattern : 'no name'))) >
            (b.name ? b.name : (b.request.url ? b.request.url : (b.request.urlPattern ? b.request.urlPattern : 'no name'))) ? 1 : -1
          );
        mappingList
          .forEach(
            value => {
              if (value.scenarioName && this.scenariosName.indexOf(value.scenarioName) === -1) {
                this.scenariosName.push(value.scenarioName)
              }
            }
          );
        this.scenariosName.sort((a, b) => a > b ? 1 : -1);
      });
  }

  detailsToggle() {
    this.displayDetails = !this.displayDetails;
  }

}
