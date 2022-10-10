/*
 * Copyright (c) 2010-2022 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {Event, Planner, PlannerHeaderEventMap, Widget} from '../index';
import $ from 'jquery';
import {PlannerDisplayMode} from './Planner';
import {EventMapOf, EventModel} from '../events/EventEmitter';

export default class PlannerHeader extends Widget {
  declare eventMap: PlannerHeaderEventMap;

  $range: JQuery<HTMLDivElement>;
  $commands: JQuery<HTMLDivElement>;
  availableDisplayModes: PlannerDisplayMode[];
  displayMode: PlannerDisplayMode;

  constructor() {
    super();

    this.availableDisplayModes = [];
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('planner-header');
    this.$range = this.$container.appendDiv('planner-range');
    this.$range.appendDiv('planner-previous').on('click', this._onPreviousClick.bind(this));
    this.$range.appendDiv('planner-today', this.session.text('ui.CalendarToday')).on('click', this._onTodayClick.bind(this));
    this.$range.appendDiv('planner-next').on('click', this._onNextClick.bind(this));
    this.$range.appendDiv('planner-select');
    this.$commands = this.$container.appendDiv('planner-commands');
    this._renderAvailableDisplayModes();
    this._renderDisplayMode();
  }

  setAvailableDisplayModes(displayModes: PlannerDisplayMode[]) {
    this.setProperty('availableDisplayModes', displayModes);
  }

  protected _renderAvailableDisplayModes() {
    let displayMode = Planner.DisplayMode;
    this.$commands.empty();

    if (this.availableDisplayModes.length > 1) {
      if (this.availableDisplayModes.indexOf(displayMode.DAY) > -1) {
        this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarDay'))
          .attr('data-mode', displayMode.DAY)
          .on('click', this._onDisplayModeClick.bind(this));
      }
      if (this.availableDisplayModes.indexOf(displayMode.WORK_WEEK) > -1) {
        this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarWorkWeek'))
          .attr('data-mode', displayMode.WORK_WEEK)
          .on('click', this._onDisplayModeClick.bind(this));
      }
      if (this.availableDisplayModes.indexOf(displayMode.WEEK) > -1) {
        this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarWeek'))
          .attr('data-mode', displayMode.WEEK)
          .on('click', this._onDisplayModeClick.bind(this));
      }
      if (this.availableDisplayModes.indexOf(displayMode.MONTH) > -1) {
        this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarMonth'))
          .attr('data-mode', displayMode.MONTH)
          .on('click', this._onDisplayModeClick.bind(this));
      }
      if (this.availableDisplayModes.indexOf(displayMode.CALENDAR_WEEK) > -1) {
        this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarCalendarWeek'))
          .attr('data-mode', displayMode.CALENDAR_WEEK)
          .on('click', this._onDisplayModeClick.bind(this));
      }
      if (this.availableDisplayModes.indexOf(displayMode.YEAR) > -1) {
        this.$commands.appendDiv('planner-mode', this.session.text('ui.CalendarYear'))
          .attr('data-mode', displayMode.YEAR)
          .on('click', this._onDisplayModeClick.bind(this));
      }
    }

    let $modes = this.$commands.children('.planner-mode');
    $modes.first().addClass('first');
    $modes.last().addClass('last');
    if ($modes.length === 1) {
      $modes.first().addClass('disabled');
      $modes.off('click');
    }
    this.$commands.appendDiv('planner-toggle-year').on('click', this._onYearClick.bind(this));
  }

  setDisplayMode(displayMode: PlannerDisplayMode) {
    this.setProperty('displayMode', displayMode);
  }

  protected _renderDisplayMode() {
    $('.planner-mode', this.$commands).select(false);
    $('[data-mode="' + this.displayMode + '"]', this.$commands).select(true);
  }

  protected _onTodayClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this.trigger('todayClick');
  }

  protected _onNextClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this.trigger('nextClick');
  }

  protected _onPreviousClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this.trigger('previousClick');
  }

  protected _onYearClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    this.trigger('yearClick');
  }

  override trigger<K extends string & keyof EventMapOf<PlannerHeader>>(type: K, eventOrModel?: Event | EventModel<EventMapOf<PlannerHeader>[K]>): EventMapOf<PlannerHeader>[K] {
    return super.trigger(type, eventOrModel);
  }

  protected _onDisplayModeClick(event: JQuery.ClickEvent<HTMLDivElement>) {
    let displayMode = $(event.target).data('mode');
    this.setDisplayMode(displayMode);
    this.trigger('displayModeClick', {
      displayMode: this.displayMode
    });
  }
}