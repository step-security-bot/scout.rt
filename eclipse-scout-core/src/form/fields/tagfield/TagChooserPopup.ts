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
import {AbstractLayout, Column, lookupField, LookupResult, Popup, scout, ScoutKeyboardEvent, Table, TableRow, TagChooserPopupEventMap, TagChooserPopupLayout, TagChooserPopupModel, TagField} from '../../../index';
import {TableRowClickEvent} from '../../../table/TableEventMap';

export default class TagChooserPopup extends Popup implements TagChooserPopupModel {
  declare model: TagChooserPopupModel;
  declare eventMap: TagChooserPopupEventMap;
  declare self: TagChooserPopup;

  table: Table;
  field: TagField;

  constructor() {
    super();
    this.table = null;
    this.windowPaddingX = 0;
    this.windowPaddingY = 0;
  }

  protected override _init(model: TagChooserPopupModel) {
    super._init(model);

    let column = scout.create(Column, {
      session: this.session,
      text: 'Tag',
      autoOptimizeWidth: false
    });

    this.table = scout.create(Table, {
      parent: this,
      headerVisible: false,
      autoResizeColumns: true,
      multiSelect: false,
      scrollToSelection: true,
      columns: [column],
      headerMenusEnabled: false,
      textFilterEnabled: false
    });

    this.table.on('rowClick', this._onRowClick.bind(this));
  }

  protected override _createLayout(): AbstractLayout {
    return new TagChooserPopupLayout(this);
  }

  protected override _render() {
    super._render();

    this.$container
      .addClass('tag-chooser-popup')
      .on('mousedown', this._onContainerMouseDown.bind(this));
    this._renderTable();
  }

  protected _renderTable() {
    this.table.setVirtual(false);
    this.table.render();

    // Make sure table never gets the focus, but looks focused
    this.table.$container
      .setTabbable(false)
      .addClass('focused');
  }

  setLookupResult(result: LookupResult<string>) {
    let lookupRows = result.lookupRows;
    this.table.deleteAllRows();
    let tableRows = lookupRows.map(lookupRow => lookupField.createTableRow(lookupRow));
    this.table.insertRows(tableRows);
  }

  protected _onRowClick(event: TableRowClickEvent) {
    this.triggerLookupRowSelected();
  }

  /**
   * This event handler is called before the mousedown handler on the _document_ is triggered
   * This allows us to prevent the default, which is important for the CellEditorPopup which
   * should stay open when the SmartField popup is closed. It also prevents the focus blur
   * event on the SmartField input-field.
   */
  protected _onContainerMouseDown(event: JQuery.MouseDownEvent): boolean {
    // when user clicks on proposal popup with table or tree (prevent default,
    // so input-field does not lose the focus, popup will be closed by the
    // proposal chooser impl.
    return false;
  }

  protected override _isMouseDownOnAnchor(event: MouseEvent): boolean {
    return this.field.$field.isOrHas(event.target as HTMLElement);
  }

  delegateKeyEvent(event: JQuery.KeyDownEvent & ScoutKeyboardEvent) {
    event.originalEvent.smartFieldEvent = true;
    this.table.$container.trigger(event);
  }

  triggerLookupRowSelected(row?: TableRow) {
    row = row || this.selectedRow();
    if (!row || !row.enabled) {
      return;
    }
    this.trigger('lookupRowSelected', {
      lookupRow: row.lookupRow
    });
  }

  selectedRow(): TableRow {
    return this.table.selectedRow();
  }
}
