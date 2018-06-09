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
import {TranslateService} from '@ngx-translate/core';
import {VersionService} from './service/version.service';
import {Version} from './model/version';
import {WiremockVersion} from './model/wiremock-version';

const LANG_TOKEN = 'bsc_autotester_language';

declare let require: any;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})

export class AppComponent implements OnInit {

  public applicationVersion: Version;
  public projectsWiremockVersions: Array<WiremockVersion>;
  langs: string[] = ['en', 'ru'];
  locale: string;
  uiVersion = require('../../package.json').version;

  constructor(private versionService: VersionService, private translate: TranslateService) {
    translate.addLangs(this.langs);
    this.locale = localStorage.getItem(LANG_TOKEN) || 'en';
    translate.setDefaultLang(this.locale);
  }

  ngOnInit(): void {
    this.versionService.getApplicationVersion().subscribe(version => this.applicationVersion = version);
    this.versionService.getProjectsWiremockVersions().subscribe(version => this.projectsWiremockVersions = version);
  }

  changeLocale(): void {
    this.translate.use(this.locale);
    this.saveCurrentLangAsDefault();
  }

  saveCurrentLangAsDefault() {
    localStorage.setItem(LANG_TOKEN, this.locale);
  }
}
