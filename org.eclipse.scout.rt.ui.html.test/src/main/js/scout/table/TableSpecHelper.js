/*******************************************************************************
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
/* global scout.MenuSpecHelper */
scout.TableSpecHelper = function(session) {
  this.session = session;
  this.menuHelper = new scout.MenuSpecHelper(session);
};

scout.TableSpecHelper.prototype.createModel = function(columns, rows) {
  var model = createSimpleModel('Table', this.session);

  //Server will never send undefined -> don't create model with undefined properties.
  if (rows) {
    model.rows = rows;
  }
  if (columns) {
    model.columns = columns;
  }

  return model;
};

scout.TableSpecHelper.prototype.createModelRow = function(id, cells) {
  return {
    id: scout.nvl(id, scout.objectFactory.createUniqueId()),
    cells: cells,
    enabled: true
  };
};

/**
 *
 * @param texts array of texts for the cells in the new row or a string if only one cell should be created.
 * @param withoutCells true if only text instead of cells should be created (server only sends text without a cell object if no other properties are set)
 */
scout.TableSpecHelper.prototype.createModelRowByTexts = function(id, texts, withoutCells) {
  texts = scout.arrays.ensure(texts);

  var cells = [];
  for (var i = 0; i < texts.length; i++) {
    if (!withoutCells) {
      cells[i] = this.createModelCell(texts[i]);
    } else {
      cells[i] = texts[i];
    }
  }
  return this.createModelRow(id, cells);
};

/**
 *
 * @param values array of values for the cells in the new row or a number if only one cell should be created.
 */
scout.TableSpecHelper.prototype.createModelRowByValues = function(id, values) {
  values = scout.arrays.ensure(values);
  var cells = [];
  for (var i = 0; i < values.length; i++) {
    cells[i] = this.createModelCell(values[i] + '', values[i]);
  }
  return this.createModelRow(id, cells);
};

scout.TableSpecHelper.prototype.createModelColumn = function(text, type) {
  var column = {
    id: scout.objectFactory.createUniqueId(),
    text: text,
    objectType: (type === undefined ? 'Column' : type),
    decimalFormat: (type === 'NumberColumn' ? new scout.DecimalFormat(this.session.locale) : undefined),
    uiSortPossible: true
  };
  scout.defaultValues.applyTo(column, 'Column');
  return column;
};

scout.TableSpecHelper.prototype.createModelCell = function(text, value) {
  var cell = {};
  scout.defaultValues.applyTo(cell, 'Cell');
  if (text !== undefined) {
    cell.text = text;
  }
  if (value !== undefined) {
    cell.value = value;
  }
  return scout.create('Cell', cell);
};

scout.TableSpecHelper.prototype.createMenuModel = function(text, icon) {
  return this.menuHelper.createModel(text, icon, ['Table.SingleSelection']);
};

scout.TableSpecHelper.prototype.createMenuModelWithSingleAndHeader = function(text, icon) {
  return this.menuHelper.createModel(text, icon, ['Table.SingleSelection', 'Table.Header']);
};

scout.TableSpecHelper.prototype.createModelColumns = function(count, columnType) {
  if (!count) {
    return;
  }

  if (!columnType) {
    columnType = 'Column';
  }

  var columns = [],
  columnTypes = [];
  if(scout.objects.isArray(columnType)){
    if(columnType.length !== count){
      throw new Error('Column count('+count+') does not match with columnType.length ('+columnType.length+').');
    }
    columnTypes = columnType;
  }else{
    for(var i = 0; i < count; i++){
      columnTypes.push(columnType);
    }
  }
  for (var j = 0; j < count; j++) {
    columns[j] = this.createModelColumn('col' + j, columnTypes[j]);
  }
  return columns;
};

/**
 * Creates cells with values.
 *
 * If the column is of type NumberColumn a numeric value is set.
 * Otherwise the value is similar to 'cell0_0' if rowId is given, or 'cell0' if no rowId is given.
 */
scout.TableSpecHelper.prototype.createModelCells = function(columns, rowId) {
  var cells = [];
  if (rowId === undefined) {
    rowId = '';
  } else {
    rowId = rowId;
  }

  if (typeof columns === 'number') {
    for (var i = 0; i < columns; i++) {
      cells[i] = this.createModelCell(rowId + '_' + i, 'cell' + rowId + '_' + i);
    }
  } else {
    for (var j = 0; j < columns.length; j++) {
      var value = 'cell' + rowId + j;
      if (columns[j].objectType === 'NumberColumn') {
        value = rowId + j;
      }
      cells[j] = this.createModelCell(rowId + '_' + j, value);
    }
  }
  return cells;
};

/**
 * Creates #rowCount rows where columns is either the column count or the column objects.
 * Passing the column objects allows to consider the column type for cell creation.
 */
scout.TableSpecHelper.prototype.createModelRows = function(columns, rowCount) {
  if (!rowCount) {
    return;
  }

  var rows = [];
  for (var i = 0; i < rowCount; i++) {
    rows[i] = this.createModelRow(null, this.createModelCells(columns, i));
  }
  return rows;
};

scout.TableSpecHelper.prototype.createModelSingleColumnByTexts = function(texts) {
  var rows = [];
  for (var i = 0; i < texts.length; i++) {
    rows.push(this.createModelRowByTexts(null, texts[i]));
  }
  return this.createModel(this.createModelColumns(1), rows);
};

scout.TableSpecHelper.prototype.createModelSingleColumnByValues = function(values, columnType) {
  var rows = [];
  for (var i = 0; i < values.length; i++) {
    rows.push(this.createModelRowByValues(null, values[i]));
  }
  return this.createModel(this.createModelColumns(1, columnType), rows);
};

scout.TableSpecHelper.prototype.createModelFixture = function(colCount, rowCount) {
  return this.createModel(this.createModelColumns(colCount), this.createModelRows(colCount, rowCount));
};

scout.TableSpecHelper.prototype.createTableWithOneColumn = function() {
  var model = this.createModelFixture(1, 1);
  return this.createTable(model);
};

scout.TableSpecHelper.prototype.createModelSingleConfiguredCheckableColumn = function(rowCount) {
  var cols = this.createModelColumns(1);
  cols[0].checkable = true;
  return this.createModel(cols, this.createModelRows(1, rowCount));
};

scout.TableSpecHelper.prototype.createTable = function(model) {
  var defaults = {
    parent: this.session.desktop
  };
  model = $.extend({}, defaults, model);
  var table = new scout.Table();
  table.init(model);
  return table;
};

scout.TableSpecHelper.prototype.createTableAdapter = function(model) {
  var tableAdapter = new scout.TableAdapter();
  tableAdapter.init(model);
  return tableAdapter;
};

scout.TableSpecHelper.prototype.createColumnFilter = function(model) {
  var filter = new scout.TextColumnUserFilter();
  filter.init(model);
  return filter;
};

scout.TableSpecHelper.prototype.createAndRegisterColumnFilter = function(model) {
  var filter = this.createColumnFilter(model);
  model.table.addFilter(filter);
  return filter;
};

/**
 * Applies display style on rows and cells so that cells are positioned correctly in a row.<br>
 * Necessary because the stylesheet is not applied when running the specs.
 */
scout.TableSpecHelper.prototype.applyDisplayStyle = function(table) {
  table.$data.css('position', 'relative');
  table.$rows().each(function() {
    var $row = $(this);
    $row.css('display', 'table-row');
    $row.children('.table-cell').each(function() {
      var $cell = $(this);
      $cell.css('display', 'table-cell');
    });
  });
};

scout.TableSpecHelper.prototype.getRowIds = function(rows) {
  var rowIds = [];
  for (var i = 0; i < rows.length; i++) {
    rowIds.push(rows[i].id);
  }
  return rowIds;
};

scout.TableSpecHelper.prototype.selectRowsAndAssert = function(table, rows) {
  table.selectRows(rows);
  this.assertSelection(table, rows);
};

scout.TableSpecHelper.prototype.assertSelection = function(table, rows) {
  var $selectedRows = table.$selectedRows();
  expect($selectedRows.length).toBe(rows.length);

  var selectedRows = [];
  $selectedRows.each(function() {
    selectedRows.push($(this).data('row'));

    if ($selectedRows.length === 1) {
      expect($(this).hasClass('select-single')).toBeTruthy();
    }
  });

  expect(scout.arrays.equalsIgnoreOrder(rows, selectedRows)).toBeTruthy();
  expect(scout.arrays.equalsIgnoreOrder(rows, table.selectedRows)).toBeTruthy();
};

/**
 * Asserts that the rows contain the given texts at column specified by colIndex
 * @param texts array with same length as rows.
 */
scout.TableSpecHelper.prototype.assertTextsInCells = function(rows, colIndex, texts) {
  expect(rows.length).toBe(texts.length);
  for (var i = 0; i < rows.length; i++) {
    expect(rows[i].cells[colIndex].text).toBe(texts[i]);
  }
};

scout.TableSpecHelper.prototype.assertValuesInCells = function(rows, colIndex, values) {
  expect(rows.length).toBe(values.length);
  for (var i = 0; i < rows.length; i++) {
    expect(rows[i].cells[colIndex].value).toBe(values[i]);
  }
};

scout.TableSpecHelper.prototype.assertDatesInCells = function(rows, colIndex, dates) {
  expect(rows.length).toBe(dates.length);
  for (var i = 0; i < rows.length; i++) {
    expect(rows[i].cells[colIndex].value.getTime()).toBe(dates[i].getTime());
  }
};

scout.TableSpecHelper.prototype.assertSelectionEvent = function(id, rowIds) {
  var event = new scout.RemoteEvent(id, 'rowsSelected', {
    rowIds: rowIds
  });
  expect(mostRecentJsonRequest()).toContainEvents(event);
};

scout.TableSpecHelper.prototype.getDisplayingContextMenu = function(table) {
  return $('body').find('.popup-body');
};

/**
 * Since scout.comparators.TEXT is a static object and only installed once,
 * we must reset the object - otherwise we could not test cases with client
 * and server side sorting.
 */
scout.TableSpecHelper.prototype.resetIntlCollator = function() {
  scout.comparators.TEXT.installed = false;
  scout.comparators.TEXT.collator = null;
};