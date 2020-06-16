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
import {Mapping, MappingList} from '../../model/mapping';
import {WireMockService} from '../../service/wire-mock.service';
import {EventService} from '../../service/event-service';
import {Router} from '@angular/router';
import {MessageService} from '../../service/message-service';
import {CheckboxStatus} from '../checkbox/checkbox.component';
import {GroupStatus} from '../../model/group-status';

@Component({
  selector: 'app-mapping-list',
  templateUrl: './mapping-list.component.html'
})
export class MappingListComponent implements OnInit {

  mappingList: MappingList;
  displayDetails = false;
  disabledDeleteSelected = true;
  disabledSelectedAll = true;
  selectedAll = false;
  isBusy = false;

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
    });
  }

  /**
   * Функция получения маппингов.
   * @description После запроса происходит сортировка и распределение по группам.
   **/
  async getMappings() {
    this.isBusy = true;
    try {
      const mappingList = await this.wireMockService.getMappingList();
      this.setMappingList(mappingList);
    } catch (e) {
      console.warn(e);
    } finally {
      this.isBusy = false;
    }
  }

  /**
   * Функция распределения маппингов.
   * @description Так как в wireMockService.getMappingList приходит список общей кучей, то для приведения к общему виду с
   * JmsMappingListComponent испольуем эту функцию.
   * Сначала сортируются маппинги, затем происходит создание групп (в mappingList.groups) и рапределение маппингов по группам.
   * Маппинги без групп попадают в общей список (mappingList.messages).
   *
   * @param {Mapping[]} mappingList
   **/
  setMappingList(mappingList) {
    this.mappingList = {
      groups: [],
      messages: []
    };

    // Сортировка маппингов
    mappingList.sort((a, b) => (a.name || a.request.url || a.request.urlPattern || 'no name')
        .localeCompare(b.name || b.request.url || b.request.urlPattern || 'no name'));

    // Распределение маппингов по группам
    mappingList.forEach(value => {
      if (!value.scenarioName) { // если нет группы
        this.mappingList.messages.push(value);
      } else if (this.mappingList.groups.findIndex(group => value.scenarioName === group.name) === -1) { // создание новой группы
        this.mappingList.groups.push({
          name: value.scenarioName,
          selected: CheckboxStatus.FALSE,
          messages: [value]
        });
      } else { // добавление в существующую группу
        const index: number = this.mappingList.groups.findIndex(group => group.name === value.scenarioName);
        const messages = this.mappingList.groups[index].messages;
        messages.push(value as Mapping);
      }
    });

    // Сортировка названий групп
    this.mappingList.groups.sort((a, b) => a.name > b.name ? 1 : -1);

    this.changeStatusButtons();
  }

  detailsToggle() {
    this.displayDetails = !this.displayDetails;
  }

  /**
   * Функция изменения состояния кнопок удаления и выделения.
   * @description Ищет есть ли выбранные маппинги и изменяет статусы кнопок.
   **/
  changeStatusButtons() {
    const predicate = el => el.selected === CheckboxStatus.TRUE;
    const isNotSelected = el => (el.selected === CheckboxStatus.FALSE || el.selected === undefined);
    const isEmpty = !(this.mappingList.messages.length + this.mappingList.groups.length);

    // Если маппингов нет, то кнопки блокируются
    if (!isEmpty) {
      this.disabledDeleteSelected = !(
        this.mappingList.messages.some(predicate) ||
        this.mappingList.groups.some(group => (group.selected !== CheckboxStatus.FALSE))
      );
      this.disabledSelectedAll = false;
      this.selectedAll = !(
        this.mappingList.messages.some(isNotSelected) ||
        this.mappingList.groups.some(group => (group.selected !== CheckboxStatus.TRUE))
      );
    } else {
      this.disabledDeleteSelected = this.disabledSelectedAll = true;
    }
  }

  /**
   * Функция выделения всех маппингов.
   * @description Отметка всех маппингов.
   * Изменение статуса кнопки "Выделить все".
   * Изменение статуса кнопки удаления.
   **/
  selectAll() {
    this.selectedAll = !this.selectedAll;

    const select = mapping => mapping.selected = CheckboxStatus.TRUE;
    const remove = mapping => mapping.selected = CheckboxStatus.FALSE;

    this.mappingList.groups.forEach(group => {
      group.selected = this.selectedAll ? CheckboxStatus.TRUE : CheckboxStatus.FALSE;
      group.messages.forEach(mapping => {
        mapping.selected = group.selected;
      });
    });
    this.mappingList.messages.forEach(this.selectedAll ? select : remove);

    this.disabledDeleteSelected = !this.selectedAll;
  }

  /**
   * Функция выделения группы маппингов.
   * @description Отметка всех зависимых маппингов.
   * Изменение статуса кнопки удаления.
   * checked приведится к логическому типу, так как может поступить значение 'indeterminate'.
   *
   * @param {CheckboxStatus} checked
   * @param {string} groupName
   **/
  selectGroup(checked: CheckboxStatus, groupName: string): void {
    const groupList = this.mappingList.groups.find(group => group.name === groupName);

    groupList.selected = checked;
    groupList.messages.forEach(mapping => {
      mapping.selected = groupList.selected;
    });

    this.changeStatusButtons();
  }

  /**
   * Функция для отметки маппинга на удаление.
   * @description Отмечает выбранный маппинг на удаление.
   * Изменяет состояние кнопки "Выделить все".
   * Разблокирует/блокирует кнопку для удаления выбранных маппингов.
   * Изменяет состояние чекбокса зависимой группы.
   *
   * @param {CheckboxStatus} checked
   * @param {Mapping} mapping
   **/
  onChange(checked: CheckboxStatus, mapping: Mapping): void {
    mapping.selected = checked;

    const mappingList = this.mappingList.groups.find(group => group.name === mapping.scenarioName);
    const isSelected = el => (el.selected !== CheckboxStatus.FALSE && el.selected !== undefined);

    if (mappingList) {
      // Проверка на checked нужна для ускорения работы, чтобы избегать лишних переборов массива
      if (checked && mappingList.messages.every(isSelected)) {
        mappingList.selected = CheckboxStatus.TRUE;
      } else if (checked || mappingList.messages.some(isSelected)) {
        mappingList.selected = CheckboxStatus.INDETERMINATE;
      } else {
        mappingList.selected = CheckboxStatus.FALSE;
      }
    }

    this.changeStatusButtons();
  }

  /**
   * Функция для подтвержения на удаление.
   * @description Если пользователь подтверждает удаление, то вызывается deleteSelected,
   * если не подтверждает, то ничего не происходит.
   **/
  confirmDeleteSelected() {
    const result: boolean = confirm('Удалить выбранные маппинги?');

    if (result) {
      this.deleteSelected();
    }
  }

  /**
  * Функция для удаления выбранных маппингов.
  * @description Из mappingList выбираются маппинги с отметкой selected.
  * Создается messageService переменная для изменения уведомлений и их состояния.
  * Блокируется кнопка удаления на время выполнения запросов.
  * Если получившийся массив selected содержит элементы, то одновременно отправляются запросы на удаление для этих маппингов.
  * По завершении, сообщение об ожидании удаляется, выводится сообщение об удалении, и,
  * в зависимости от страницы, либо осуществляется переход на /mapping, либо обновляется список маппингов.
  * Если список selected пуст, то выводится предупреждение.
  **/
  async deleteSelected() {
    // Выбор из mappings.messages маппингов на удаление
    let selected: Mapping[] = this.mappingList.messages.filter(mapping => mapping.selected);
    // Выбор из mappings.groups маппингов на удаление
    if (this.mappingList.groups) {
      this.mappingList.groups.forEach(group => {
        selected = selected.concat(group.messages.filter(mapping => mapping.selected));
      });
    }

    let messageService;

    if (selected && selected.length) {
      try {
        this.isBusy = true;
        // Добавление сообщения об ожидании
        messageService = this.messageService.wait('Удаление', 'Удаление может занять некоторое время');

        await Promise.all(selected.map(mapping => this.wireMockService.deleteOne(mapping)));
        await this.wireMockService.saveToBackStorage();
      } catch (e) {
        this.messageService.error('Что-то пошло не так');
        console.warn(e);
      } finally {
        if (this.router.isActive('/mapping', true)) {
          await this.getMappings();
        } else {
          await this.router.navigate(['/mapping']);
        }

        // Удаление сообщения об ожидании
        messageService.clear(messageService.uniqueCounter);
        this.messageService.success('Маппинги успешно удалены');

        this.isBusy = false;
        this.changeStatusButtons();
      }
    } else {
      this.messageService.error('Не выбраны маппинги');
    }
  }
}
