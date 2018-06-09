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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import {TranslateModule, TranslateLoader} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

import { AppComponent } from './app.component';
import {ToastyModule} from 'ng2-toasty';
import {RouterModule, Routes} from '@angular/router';
import { ProjectListComponent } from './project-list/project-list.component';
import { ProjectDetailComponent } from './project-detail/project-detail.component';
import { ScenarioDetailComponent } from './scenario-detail/scenario-detail.component';
import {DiffComponent} from './shared/diff/diff.component';
import {ProjectService} from './service/project.service';
import {Http, HttpModule} from '@angular/http';
import { ScenarioListItemComponent } from './scenario-list-item/scenario-list-item.component';
import { ScenarioTitleItemComponent } from './scenario-list-item/scenario-title-item.component';
import {ScenarioService} from './service/scenario.service';
import {StepService} from './service/step.service';
import { StepResultItemComponent } from './step-result-item/step-result-item.component';
import { StepItemComponent } from './step-item/step-item.component';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import { MockServiceResponseComponent } from './mock-service-response/mock-service-response.component';
import { StepParameterSetComponent } from './step-parameter-set/step-parameter-set.component';
import {Globals} from './globals';
import { ProjectSettingsComponent } from './project-settings/project-settings.component';
import { ScenarioSettingsComponent } from './scenario-settings/scenario-settings.component';
import {VersionService} from './service/version.service';
import {CustomToastyService} from './service/custom-toasty.service';
import { SearchComponent } from './search-scenario/search-scenario.component';
import {SearchScenarioService} from './service/search-scenario.service';
import {SyncScrollDirective} from './shared/directives/sync-scroll.directive';
import {TextSelectDirective} from './shared/directives/text-select.directive';
import { HelpComponent } from './help/help.component';
import { MqMockResponseComponent } from './mq-mock-response/mq-mock-response.component';
import { TextareaAutosizeModule } from 'ngx-textarea-autosize';

export function HttpLoaderFactory(http: Http) {
  return new TranslateHttpLoader(http);
}

const routes: Routes = [
  { path: '', component: ProjectListComponent },
  { path: 'help', component: HelpComponent },
  { path: 'project/:projectCode', component: ProjectDetailComponent },
  { path: 'project/:projectCode/settings', component: ProjectSettingsComponent },
  { path: 'project/:projectCode/scenario/:scenarioCode/settings', component: ScenarioSettingsComponent },
  { path: 'project/:projectCode/scenario/:scenarioGroup/:scenarioCode/settings', component: ScenarioSettingsComponent },
  { path: 'project/:projectCode/scenario/:scenarioCode', component: ScenarioDetailComponent },
  { path: 'project/:projectCode/scenario/:scenarioGroup/:scenarioCode', component: ScenarioDetailComponent },

];

@NgModule({
  declarations: [
    AppComponent,
    ProjectListComponent,
    ProjectDetailComponent,
    ScenarioDetailComponent,
    ScenarioListItemComponent,
    ScenarioTitleItemComponent,
    StepResultItemComponent,
    StepItemComponent,
    MockServiceResponseComponent,
    StepParameterSetComponent,
    ProjectSettingsComponent,
    ScenarioSettingsComponent,
    DiffComponent,
    SyncScrollDirective,
    SearchComponent,
    TextSelectDirective,
    HelpComponent,
    MqMockResponseComponent
  ],
  imports: [
    BrowserModule,
    HttpModule,
    RouterModule.forRoot(routes, { useHash: true }),
    ToastyModule.forRoot(),
    TranslateModule.forRoot({
      loader: {
        provide: TranslateLoader,
        useFactory: HttpLoaderFactory,
        deps: [Http]
      }
    }),
    FormsModule,
    ReactiveFormsModule,
    TextareaAutosizeModule
  ],
  providers: [ProjectService, ScenarioService, StepService, CustomToastyService, VersionService, Globals, SearchScenarioService],
  bootstrap: [AppComponent]
})
export class AppModule { }
