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
import {Status, strings, ValueField} from '../index';
import $ from 'jquery';
import CellModel from './CellModel';

/**
 * -1 for left, 0 for center and 1 for right.
 */
export type HorizontalAlignment = -1 | 0 | 1;

export default class Cell implements CellModel {
  declare model: CellModel;

  cssClass: string;
  editable: boolean;
  errorStatus: Status;
  horizontalAlignment: HorizontalAlignment;
  htmlEnabled: boolean;
  iconId: string;
  mandatory: boolean;
  text: string;
  flowsLeft: boolean;
  empty: boolean;
  value: any;
  tooltipText: string;
  foregroundColor: string;
  backgroundColor: string;
  font: string;
  sortCode: number;
  field: ValueField;
  protected _cachedEncodedText: string;

  constructor() {
    this.cssClass = null;
    this.editable = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this.errorStatus = null;
    this.horizontalAlignment = null; /* do not initialize with -1. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this.htmlEnabled = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this.iconId = null;
    this.mandatory = null; /* do not initialize with false. This is required because there's a subtle logic in Column.js (_initCell) which checks if a value is set. */
    this._cachedEncodedText = null;
    this.text = null;
    this.value = null;
    this.tooltipText = null;
    this.sortCode = null;
  }

  init(model: CellModel) {
    this._init(model);
  }

  protected _init(model: CellModel) {
    $.extend(this, model);
  }

  update(model: CellModel) {
    this.setText(model.text);
    $.extend(this, model);
  }

  setEditable(editable: boolean) {
    this.editable = editable;
  }

  setMandatory(mandatory: boolean) {
    this.mandatory = mandatory;
  }

  setHorizontalAlignment(hAlign: HorizontalAlignment) {
    this.horizontalAlignment = hAlign;
  }

  setValue(value: any) {
    this.value = value;
  }

  setErrorStatus(errorStatus: Status) {
    this.errorStatus = errorStatus;
  }

  setText(text: string) {
    let oldText = this.text;
    this.text = text;

    // reset cached encodedText, so when encodedText() is called the next time
    // it will be set to the a new value
    if (oldText !== this.text) {
      this._cachedEncodedText = null;
    }
  }

  setIconId(iconId: string) {
    this.iconId = iconId;
  }

  encodedText() {
    if (!this._cachedEncodedText) {
      // Encode text and cache it, encoding is expensive
      this._cachedEncodedText = strings.encode(this.text);
    }
    return this._cachedEncodedText;
  }

  setCssClass(cssClass: string) {
    this.cssClass = cssClass;
  }

  setSortCode(sortCode: number) {
    this.sortCode = sortCode;
  }
}
