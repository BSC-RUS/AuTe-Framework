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
import {JmsMapping, JmsMappings} from '../../model/jms-mapping';
import {Router} from '@angular/router';
import {MessageService} from '../../service/message-service';

@Component({
  selector: 'app-jms-mapping-list',
  templateUrl: './jms-mapping-list.component.html'
})
export class JmsMappingListComponent implements OnInit {

  mappings: JmsMappings;
  displayDetails = false;
  disabledDeleteSelected = true;
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

  getMappings() {
    this.wireMockService.getJmsMappings().then(mappings => this.mappings = mappings);
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
   * @param {JmsMapping} mapping
   **/
  onChange(checked: boolean, mapping: JmsMapping): void {
    mapping.selected = checked;

    this.disabledDeleteSelected = !(
      (this.mappings.messages && (this.mappings.messages.some(el => el.selected))) ||
      (this.mappings.groups && this.mappings.groups.length &&
        this.mappings.groups.some(group => group.messages.some(el => el.selected)))
    );
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
   * @description Из mappings выбираются маппинги с отметкой selected.
   * Создается messageService переменная для изменения уведомлений и их состояния.
   * Блокируется кнопка удаления на время выполнения запросов.
   * Если получившийся массив selected содержит элементы, то одновременно отправляются запросы на удаление для этих маппингов.
   * По завершении, сообщение об ожидании удаляется, выводится сообщение об удалении, и,
   * в зависимости от страницы, либо осуществляется переход на /jms-mapping, либо обновляется список маппингов.
   * Если список selected пуст, то выводится предупреждение.
   **/
  async deleteSelected() {
    // Выбор из mappings.messages маппингов на удаление
    let selected: JmsMapping[] = this.mappings.messages.filter(mapping => mapping.selected);
    // Выбор из mappings.groups маппингов на удаление
    if (this.mappings.groups) {
      this.mappings.groups.forEach(group => {
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
          this.getMappings();
        } else {
          await this.router.navigate(['/jms-mapping']);
        }

        // Удаление сообщения об ожидании
        messageService.clear(messageService.uniqueCounter);
        this.messageService.success('Маппинги успешно удалены');

        this.isBusy = false;
        this.disabledDeleteSelected = true;
      }
    } else {
      this.messageService.error('Не выбраны JMS-маппинги');
    }
  }
}
