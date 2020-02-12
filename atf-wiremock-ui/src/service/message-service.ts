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

import {ToastyService} from 'ng2-toasty';
import {Injectable} from '@angular/core';

@Injectable()
export class MessageService {
  /**
   * Набор опций для вывода сообщей для toastyService
   *
   * @param {string} title Принимаемое сообщение
   * @return {object}
   **/
  private static toastyOptions(title: string) {
    return {
      title,
      showClose: true,
      timeout: 5000,
      theme: 'bootstrap'
    };
  }

  constructor(
    private readonly toastyService: ToastyService
  ) {
  }

  success(title: string) {
    this.toastyService.success(MessageService.toastyOptions(title));
  }

  error(title: string) {
    this.toastyService.error(MessageService.toastyOptions(title));
  }
}
