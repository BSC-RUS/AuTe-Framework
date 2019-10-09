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
import {JmsMapping} from "../../model/jms-mapping";
import {WireMockService} from "../../service/wire-mock.service";
import {ActivatedRoute, Router} from "@angular/router";
import {JmsResponse} from "../../model/jms-response";
import {EventService} from "../../service/event-service";
import {ToastOptions, ToastyService} from "ng2-toasty";

@Component({
  selector: 'app-jms-mapping-detail',
  templateUrl: './jms-mapping-detail.component.html',
})
export class JmsMappingDetailComponent implements OnInit {

  mapping: JmsMapping;

  constructor(
    public wiremock: WireMockService,
    private eventService: EventService,
    private route: ActivatedRoute,
    private router: Router,
    private toastyService: ToastyService,
  ) {
  }

  ngOnInit() {
    this.route.paramMap.switchMap(params => {
      return params.get('guid') === 'new' ?
        this.newJmsMapping() :
        this.wiremock.findJmsMapping(params.get('guid'));
    }).subscribe(mapping => {
      this.mapping = mapping;
    });
  }

  newJmsMapping() {
    this.mapping = new JmsMapping();
    this.addResponse();
    return new Promise<JmsMapping>(() => {
    });
  }

  applyMapping() {
    this.wiremock
      .applyJms(this.mapping)
      .then(guid => {
        this.mapping.guid = guid;
        this.toastyService.success(this.toastyOptions('JMS маппинг применен'));
        // noinspection JSIgnoredPromiseFromCall
        this.router.navigate(['/jms-mapping', guid]);
        this.eventService.updateJmsMappingList.next(true);
      }).catch(() => {
      this.toastyService.error(this.toastyOptions('Не удалось применить'));
    })
  }

  deleteMock() {
    if (confirm('Confirm: delete mock')) {
      this.wiremock
        .deleteJmsMapping(this.mapping)
        .then(() => {
          this.toastyService.success(this.toastyOptions('JMS маппинг удален'));
          // noinspection JSIgnoredPromiseFromCall
          this.router.navigate(['/jms-mapping']);
        });
    }
  }

  addResponse() {
    if (!this.mapping.responses) {
      this.mapping.responses = []
    }
    this.mapping.responses.push(new JmsResponse());
  }

  deleteResponse(response: JmsResponse) {
    this.mapping.responses = this.mapping.responses.filter(r => r !== response);
  }

  toastyOptions(title: string): ToastOptions {
    return {
      title: title,
      showClose: true,
      timeout: 5000,
      theme: 'bootstrap'
    }
  }
}
