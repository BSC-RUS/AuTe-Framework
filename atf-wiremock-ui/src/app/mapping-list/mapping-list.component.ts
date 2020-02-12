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
import {EventService} from '../../service/event-service';
import {Router} from '@angular/router';
import {MessageService} from '../../service/message-service';

@Component({
  selector: 'app-mapping-list',
  templateUrl: './mapping-list.component.html'
})
export class MappingListComponent implements OnInit {

  mappingList: Mapping[];
  displayDetails = false;
  scenariosName = [];
  disabledDeleteSelected = true;

  constructor(
    public wireMockService: WireMockService,
    private eventService: EventService,
    private router: Router,
    private messageService: MessageService
  ) { }

  ngOnInit() {
    this.getMappings();
    this.eventService.updateMappingList.subscribe(value => {
      if (value) {
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

  /**
   * Функция для отметки маппинга на удаление.
   * @description Отмечает выбранный маппинг на удаление.
   * Разблокирует/блокирует кнопку для удаления выбранных маппингов.
   *
   * @param {boolean} checked
   * @param {Mapping} mapping
   **/
  onChange(checked: boolean, mapping: Mapping): void {
    mapping.selected = checked;

    this.disabledDeleteSelected = !(this.mappingList.some(el => el.selected));
  }

  /**
  * Функция для удаления выбранных маппингов.
  * @description Из mappingList выбираются маппинги с отметкой selected.
  * Если получившийся массив selected содержит элементы, то одновременно отправляются запросы на удаление для этих маппингов
  * потом в зависимости от страницы либо осуществляется переход на /mapping, либо обновляется список маппингов.
  * Если список selected пуст, то выводится предупреждение.
  **/
  async deleteSelected() {
    const selected: Mapping[] = this.mappingList.filter(mapping => mapping.selected);

    if (selected && selected.length) {
      try {
        await Promise.all(selected.map(mapping => this.wireMockService.deleteOne(mapping)));
        this.wireMockService.saveToBackStorage().then(() => {
          this.messageService.success('Маппинги успешно удалены');
        });
        if (this.router.isActive('/mapping', true)) {
          this.getMappings();
        } else {
          await this.router.navigate(['/mapping']);
        }
      } catch (e) {
        this.messageService.error('Что-то пошло не так');
        console.warn(e);
      }
    } else {
      this.messageService.error('Не выбраны маппинги');
    }
  }
}
