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
   * Функция для удаления выбранных маппингов.
   * @description Из mappings выбираются маппинги с отметкой selected.
   * Если получившийся массив selected содержит элементы, то одновременно отправляются запросы на удаление для этих маппингов
   * потом в зависимости от страницы либо осуществляется переход на /jms-mapping, либо обновляется список маппингов.
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

    if (selected && selected.length) {
      try {
        await Promise.all(selected.map(mapping => this.wireMockService.deleteJmsMapping(mapping)));
        this.wireMockService.saveToBackStorage().then(() => {
          this.messageService.success('JMS-маппинги успешно удалены');
        });
        if (this.router.isActive('/jms-mapping', true)) {
          this.getMappings();
        } else {
          await this.router.navigate(['/jms-mapping']);
        }
      } catch (e) {
        this.messageService.error('Что-то пошло не так');
        console.warn(e);
      }
    } else {
      this.messageService.error('Не выбраны JMS-маппинги');
    }
  }
}
