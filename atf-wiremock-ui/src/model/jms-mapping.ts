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

import {JmsResponse} from './jms-response'
import {GroupStatus} from './group-status';
import {CheckboxStatus} from '../app/checkbox/checkbox.component';

export class JmsMappingList {
  groups: GroupStatus<JmsMapping>[] = [];
  messages: JmsMapping[] = [];
}

export class JmsMapping {
  guid: string;
  name: string;
  group: string; // аналог scenarioName в Mapping
  sourceQueueName: string;
  responses: JmsResponse[] = [];
  httpUrl: string;
  xpath: string;
  priority: number;
  selected?: CheckboxStatus; // select for delete in jms-mapping-list
}
