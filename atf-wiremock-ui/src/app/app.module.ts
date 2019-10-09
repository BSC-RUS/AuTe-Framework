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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppComponent } from './app.component';
import {WireMockService} from '../service/wire-mock.service';
import {HttpModule} from '@angular/http';
import {MappingDetailComponent} from './mapping-detail/mapping-detail.component';
import {FormsModule} from '@angular/forms';
import { InputNullComponent } from './input-null/input-null.component';
import {RouterModule, Routes} from '@angular/router';
import {ToastyModule} from 'ng2-toasty';
import { RequestListComponent } from './requests/request-list.component';
import { MappingListComponent } from './mapping-list/mapping-list.component';
import ObjNgFor from './pipe/obj-ng-for-pipe';
import { MqLogComponent } from './mq-log/mq-log.component';
import {MqMockerService} from '../service/mq-mocker-service';
import {EventService} from "../service/event-service";
import { JmsMappingListComponent } from './jms-mapping-list/jms-mapping-list.component';
import { JmsMappingDetailComponent } from './jms-mapping-detail/jms-mapping-detail.component';

const routes: Routes = [
  { path: 'requests', component: RequestListComponent },
  { path: 'mq-log', component: MqLogComponent },
  { path: 'mapping', component: MappingListComponent },
  { path: 'jms-mapping', component: JmsMappingListComponent },
  { path: 'mapping/:uuid', component: MappingDetailComponent },
  { path: 'jms-mapping/:guid', component: JmsMappingDetailComponent }
];

@NgModule({
  declarations: [
    AppComponent,
    MappingDetailComponent,
    InputNullComponent,
    RequestListComponent,
    MappingListComponent,
    ObjNgFor,
    MqLogComponent,
    JmsMappingListComponent,
    JmsMappingDetailComponent
  ],
  imports: [
    BrowserModule,
    HttpModule,
    FormsModule,
    RouterModule.forRoot(routes, { useHash: true }),
    ToastyModule.forRoot()
  ],
  providers: [WireMockService, MqMockerService, EventService],
  bootstrap: [AppComponent]
})
export class AppModule { }
