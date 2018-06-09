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
import {WireMockService} from '../service/wire-mock.service';
import {ToastOptions, ToastyService} from 'ng2-toasty';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent implements OnInit {

  window = window;

  constructor(
    public wireMockService: WireMockService,
    private toastyService: ToastyService
  ) { }

  ngOnInit(): void {}

  saveToBackStorage() {
    if (confirm('Confirm saving')) {
      this.wireMockService.saveToBackStorage().then(() => {
        const toastOptions: ToastOptions = {
          title: 'Saved',
          msg: 'Маппинги сохранены на диск',
          showClose: true,
          timeout: 5000,
          theme: 'bootstrap'
        };
        this.toastyService.success(toastOptions)
      });
    }
  }
}
