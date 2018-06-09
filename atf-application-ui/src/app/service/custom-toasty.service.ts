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

import { Injectable } from '@angular/core';
import {ToastyService} from 'ng2-toasty';

@Injectable()
export class CustomToastyService {

  constructor(
    private toastyService: ToastyService
  ) { }

  saving(title?: string, msg?: string): number {
    this.toastyService.clearAll();
    this.toastyService.warning({
      title: title ? title : 'Сохранение',
      msg: msg ? msg : 'Сохранение может занять некоторое время...',
      timeout: 100000,
      showClose: true,
      theme: 'bootstrap'
    });
    return this.toastyService.uniqueCounter;
  }

  deletion(title?: string, msg?: string) {
    this.toastyService.clearAll();
    this.toastyService.warning({
      title: title ? title : 'Удаление',
      msg: msg ? msg : 'Удаление может занять некоторое время...',
      timeout: 100000,
      showClose: true,
      theme: 'bootstrap'
    });
    return this.toastyService.uniqueCounter;
  }

  success(title: string, msg: string): number {
    this.toastyService.success({
      title: title,
      msg: msg,
      showClose: true,
      timeout: 5000,
      theme: 'bootstrap'
    });
    return this.toastyService.uniqueCounter;
  }

  error(title: string, msg: string): number {
    this.toastyService.error({
      title: title,
      msg: msg,
      timeout: 500000,
      showClose: true,
      theme: 'bootstrap'
    });
    return this.toastyService.uniqueCounter;
  }

  clear(id: number): void {
    this.toastyService.clear(id);
  }

}
