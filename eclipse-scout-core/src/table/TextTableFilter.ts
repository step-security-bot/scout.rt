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
import {strings, TableRow, TextFilter} from '../index';

export class TextTableFilter implements TextFilter<TableRow> {
  acceptedText: string;
  textSupplier: (row: TableRow) => string;
  active: boolean;
  alwaysAcceptedRowIds: string[];

  /**
   * @param textSupplier - An optional function that extracts the text from a table row. The default expects the row object to have a "lookupRow" property and returns the "text" property.
   */
  constructor(textSupplier?: (row: TableRow) => string) {
    this.acceptedText = null;
    this.textSupplier = textSupplier || (row => row.lookupRow && row.lookupRow.text);
    this.active = true;
    this.alwaysAcceptedRowIds = [];
  }

  accept(row: TableRow): boolean {
    if (!this.active || strings.empty(this.acceptedText) || this.alwaysAcceptedRowIds.indexOf(row.id) !== -1) {
      return true;
    }
    let text = this.textSupplier(row);
    if (strings.empty(text)) {
      return false;
    }
    return strings.contains(text.toLowerCase(), this.acceptedText.toLowerCase());
  }

  setAcceptedText(acceptedText: string) {
    this.acceptedText = acceptedText;
  }

  setActive(active: boolean) {
    this.active = active;
  }

  setAlwaysAcceptedRowIds(...rowIds: string[]) {
    this.alwaysAcceptedRowIds = rowIds;
  }
}
