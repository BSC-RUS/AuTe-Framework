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

import {Directive, ElementRef, HostListener, Input, AfterViewInit} from '@angular/core';

@Directive({
  selector: '[appSyncScroll]',
})
export class SyncScrollDirective implements AfterViewInit {
  @Input('appSyncScroll') boundElement: any;

  elemScrollDistance: number;
  boundElemScrollDistance: number;

  @HostListener('mouseenter') allowScroll() {
    this.ref.nativeElement.disableScrollEvent = false;
  }

  @HostListener('scroll') synchronizeScroll() {
    if (this.boundElement && !this.ref.nativeElement.disableScrollEvent
      && this.elemScrollDistance > 0 && this.boundElemScrollDistance > 0) {
      const scrolled: number = this.ref.nativeElement.scrollTop / this.elemScrollDistance;
      this.boundElement.scrollTop = scrolled * this.boundElemScrollDistance;
      this.boundElement.disableScrollEvent = true;
    }
  }

  constructor(private ref: ElementRef) {}

  ngAfterViewInit() {
    this.elemScrollDistance = this.ref.nativeElement.scrollHeight - this.ref.nativeElement.clientHeight;
    this.boundElemScrollDistance = this.boundElement.scrollHeight - this.boundElement.clientHeight;
  }
}
