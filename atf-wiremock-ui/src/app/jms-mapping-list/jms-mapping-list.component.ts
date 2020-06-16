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
import {WireMockService} from '../../service/wire-mock.service';
import {EventService} from '../../service/event-service';
import {JmsMapping, JmsMappingList} from '../../model/jms-mapping';
import {Router} from '@angular/router';
import {MessageService} from '../../service/message-service';
import {CheckboxStatus} from '../checkbox/checkbox.component';

@Component({
  selector: 'app-jms-mapping-list',
  templateUrl: './jms-mapping-list.component.html'
})
export class JmsMappingListComponent implements OnInit {

  mappingList: JmsMappingList;
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

  /**
   * Функция получения маппингов.
   * @description Получение маппингов.
   **/
  async getMappings() {
    this.isBusy = true;
    try {
      this.mappingList = await this.wireMockService.getJmsMappings();
    } catch (e) {
      console.warn(e);
    } finally {
      this.isBusy = false;
      this.changeStatusButtons();
    }
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
        (this.mappingList.groups.some(group => (group.selected !== undefined && group.selected !== CheckboxStatus.FALSE)))
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
   * @param {JmsMapping} mapping
   **/
  onChange(checked: CheckboxStatus, mapping: JmsMapping): void {
    mapping.selected = checked;

    const mappingList = this.mappingList.groups.find(group => group.name === mapping.group);
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
    const result: boolean = confirm('Удалить выбранные JMS-маппинги?');

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
   * в зависимости от страницы, либо осуществляется переход на /jms-mapping, либо обновляется список маппингов.
   * Если список selected пуст, то выводится предупреждение.
   **/
  async deleteSelected() {
    // Выбор из mappingList.messages маппингов на удаление
    let selected: JmsMapping[] = this.mappingList.messages.filter(mapping => mapping.selected);
    // Выбор из mappingList.groups маппингов на удаление
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

        await Promise.all(selected.map(mapping => this.wireMockService.deleteJmsMapping(mapping)));
        await this.wireMockService.saveToBackStorage();
      } catch (e) {
        this.messageService.error('Что-то пошло не так');
        console.warn(e);
      } finally {
        if (this.router.isActive('/jms-mapping', true)) {
          await this.getMappings();
        } else {
          await this.router.navigate(['/jms-mapping']);
        }

        // Удаление сообщения об ожидании
        messageService.clear(messageService.uniqueCounter);
        this.messageService.success('Маппинги успешно удалены');

        this.isBusy = false;
        this.changeStatusButtons();
      }
    } else {
      this.messageService.error('Не выбраны JMS-маппинги');
    }
  }
}
