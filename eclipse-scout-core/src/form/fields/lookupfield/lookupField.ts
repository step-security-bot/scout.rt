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
import {Cell, LookupRow, scout, TableRow} from '../../../index';
import {ModelOf} from '../../../scout';

/**
 * Creates a table-row for the given lookup-row.
 */
export function createTableRow(lookupRow: LookupRow<any>, multipleColumns?: boolean): ModelOf<TableRow> {
  multipleColumns = scout.nvl(multipleColumns, false);
  let cells: Cell[] = [],
    row: ModelOf<TableRow> = {
      cells: cells,
      lookupRow: lookupRow
    };
  if (lookupRow.enabled === false) {
    row.enabled = false;
  }
  if (lookupRow.cssClass) {
    row.cssClass = lookupRow.cssClass;
  }
  if (lookupRow.active === false) {
    row.cssClass = (row.cssClass ? (row.cssClass + ' ') : '') + 'inactive';
  }

  if (!multipleColumns) {
    cells.push(createTableCell(lookupRow, null, null));
  }

  return row;
}

/**
 * Creates a table cell for a descriptor. If no descriptor is provided, the default lookupRow cell is created.
 */
export function createTableCell<T>(lookupRow: LookupRow<T>, desc?: { propertyName?: string }, tableRowData?: object): Cell<T> {
  let cell = scout.create(Cell);

  // default column descriptor (first column) has propertyName null
  if (!(desc && desc.propertyName)) {
    cell.text = lookupRow.text;
    if (lookupRow.iconId) {
      cell.iconId = lookupRow.iconId;
    }
    if (lookupRow.tooltipText) {
      cell.tooltipText = lookupRow.tooltipText;
    }
    if (lookupRow.backgroundColor) {
      cell.backgroundColor = lookupRow.backgroundColor;
    }
    if (lookupRow.foregroundColor) {
      cell.foregroundColor = lookupRow.foregroundColor;
    }
    if (lookupRow.font) {
      cell.font = lookupRow.font;
    }
  } else {
    cell.text = tableRowData[desc.propertyName];
  }

  return cell;
}

export default {
  createTableCell,
  createTableRow
};
