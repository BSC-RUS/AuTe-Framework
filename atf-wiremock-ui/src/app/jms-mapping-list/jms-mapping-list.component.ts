/*
 * AuTe Framework project
 * Copyright 2018 BSC Msc, LLC
 *
 * ATF project is licensed under
 *     The Apache 2.0 License
 *     http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * For more information visit http://www.bsc-ideas.com/ru/
 *
 * Files ru.bsc.test.autotester.diff.DiffMatchPatch.java, ru.bsc.test.autotester.diff.Diff.java,
 * ru.bsc.test.autotester.diff.LinesToCharsResult, ru.bsc.test.autotester.diff.Operation,
 * ru.bsc.test.autotester.diff.Patch
 * are copied from https://github.com/google/diff-match-patch
 */

import {Component, OnInit} from '@angular/core';
import {WireMockService} from "../../service/wire-mock.service";
import {EventService} from "../../service/event-service";
import {JmsMappings} from "../../model/jms-mapping";


@Component({
  selector: 'app-jms-mapping-list',
  templateUrl: './jms-mapping-list.component.html'
})
export class JmsMappingListComponent implements OnInit {

  mappings: JmsMappings;
  displayDetails = false;

  constructor(
    public wireMockService: WireMockService,
    private eventService: EventService
  ) {
  }

  ngOnInit() {
    this.getMappings();
    this.eventService.updateJmsMappingList.subscribe(value => {
      if (value) {
        this.getMappings();
      }
    });
  }

  getMappings() {
    this.wireMockService.getJmsMappings().then(mappings => this.mappings = mappings);
  }

  detailsToggle() {
    this.displayDetails = !this.displayDetails;
  }
}
