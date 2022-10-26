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
import {LogicalGridLayout} from '../../index';

export interface LogicalGridLayoutConfigModel {
  /**
   * The horizontal gap in pixels to use between two logical grid columns.
   */
  hgap?: number;
  /**
   * The vertical gap in pixels to use between two logical grid rows.
   */
  vgap?: number;
  /**
   * The width in pixels to use for elements with the logical unit "width = 1".
   * Larger logical widths are multiplied with this value (and gaps are added).
   */
  columnWidth?: number;
  /**
   * The height in pixels to use for elements with the logical unit "height = 1".
   * Larger logical heights are multiplied with this value (and gaps are added).
   */
  rowHeight?: number;
  /**
   * The minimum width of the widget.
   * If this width is > 0 a horizontal scrollbar is shown when the widgets gets smaller than this value.
   */
  minWidth?: number;
}

/**
 * Configures layouting hints for elements layouted by {@link LogicalGridLayout}.
 *
 * The configured hints only have an effect if theirs value is >=0.
 * Otherwise, the default values specified by CSS are applied (see {@link LogicalGridLayout._initDefaults}).
 */
export default class LogicalGridLayoutConfig implements LogicalGridLayoutConfigModel {
  hgap: number;
  vgap: number;
  columnWidth: number;
  rowHeight: number;
  minWidth: number;

  constructor(options?: LogicalGridLayoutConfigModel) {
    // -1 means use the UI defaults
    options = options || {};
    if (options.hgap > -1) {
      this.hgap = options.hgap;
    }
    if (options.vgap > -1) {
      this.vgap = options.vgap;
    }
    if (options.columnWidth > -1) {
      this.columnWidth = options.columnWidth;
    }
    if (options.rowHeight > -1) {
      this.rowHeight = options.rowHeight;
    }
    if (options.minWidth > -1) {
      this.minWidth = options.minWidth;
    }
  }

  clone(): LogicalGridLayoutConfig {
    return new LogicalGridLayoutConfig(this);
  }

  applyToLayout(layout: LogicalGridLayout) {
    layout.layoutConfig = this;
    if (this.hgap !== null && this.hgap !== undefined) {
      layout.hgap = this.hgap;
    }
    if (this.vgap !== null && this.vgap !== undefined) {
      layout.vgap = this.vgap;
    }
    if (this.columnWidth !== null && this.columnWidth !== undefined) {
      layout.columnWidth = this.columnWidth;
    }
    if (this.rowHeight !== null && this.rowHeight !== undefined) {
      layout.rowHeight = this.rowHeight;
    }
    if (this.minWidth !== null && this.minWidth !== undefined) {
      layout.minWidth = this.minWidth;
    }
  }

  static ensure(layoutConfig: LogicalGridLayoutConfig | LogicalGridLayoutConfigModel): LogicalGridLayoutConfig {
    if (!layoutConfig) {
      return null;
    }
    if (layoutConfig instanceof LogicalGridLayoutConfig) {
      return layoutConfig;
    }
    return new LogicalGridLayoutConfig(layoutConfig);
  }
}
