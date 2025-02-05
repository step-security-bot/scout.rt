/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {AbstractLayout, Dimension, EventHandler, FormField, graphics, HtmlComponent, HtmlCompPrefSizeOptions, HtmlEnvironment, TabBox} from '../../../index';

export class TabBoxLayout extends AbstractLayout {
  htmlPropertyChangeHandler: EventHandler;
  protected _tabBox: TabBox;
  private _statusWidth: number;

  constructor(tabBox: TabBox) {
    super();
    this._tabBox = tabBox;
    this._statusWidth = null;

    this._initDefaults();

    this.htmlPropertyChangeHandler = this._onHtmlEnvironmentPropertyChange.bind(this);
    HtmlEnvironment.get().on('propertyChange', this.htmlPropertyChangeHandler);
    this._tabBox.one('remove', () => {
      HtmlEnvironment.get().off('propertyChange', this.htmlPropertyChangeHandler);
    });
  }

  protected _initDefaults() {
    this._statusWidth = HtmlEnvironment.get().fieldStatusWidth;
  }

  protected _onHtmlEnvironmentPropertyChange() {
    this._initDefaults();
    this._tabBox.invalidateLayoutTree();
  }

  override layout($container: JQuery) {
    let containerSize, tabContentSize, headerMargins, innerHeaderSize,
      htmlContainer = HtmlComponent.get($container),
      htmlTabContent = HtmlComponent.get(this._tabBox._$tabContent),
      htmlHeader = HtmlComponent.get(this._tabBox.header.$container),
      headerWidthHint = 0,
      headerSize = new Dimension(),
      tooltip = this._tabBox.tooltip(),
      $status = this._tabBox.$status,
      statusPosition = this._tabBox.statusPosition;

    containerSize = htmlContainer.availableSize()
      .subtract(htmlContainer.insets());

    if (htmlHeader.isVisible()) {
      headerMargins = htmlHeader.margins();
      headerWidthHint = containerSize.subtract(headerMargins).width;
      if ($status && $status.isVisible()) {
        this._layoutStatus();
        if (statusPosition === FormField.StatusPosition.DEFAULT) {
          headerWidthHint -= (this._statusWidth + graphics.margins($status).horizontal());
        }
      }
      innerHeaderSize = htmlHeader.prefSize({
        widthHint: headerWidthHint
      });

      if ($status && $status.isVisible()) {
        this._layoutStatus(innerHeaderSize.height);
      }

      innerHeaderSize.width = headerWidthHint;
      htmlHeader.setSize(innerHeaderSize);
      headerSize = innerHeaderSize.add(headerMargins);
    }

    tabContentSize = containerSize.subtract(htmlTabContent.margins());
    tabContentSize.height -= headerSize.height;
    htmlTabContent.setSize(tabContentSize);

    // Make sure tooltip is at correct position after layouting, if there is one
    if (tooltip && tooltip.rendered) {
      tooltip.position();
    }
  }

  protected _layoutStatus(height = 0) {
    let htmlContainer = this._tabBox.htmlComp,
      containerPadding = htmlContainer.insets({
        includeBorder: false
      }),
      top = containerPadding.top,
      right = containerPadding.right,
      $header = this._tabBox.header.$container,
      $status = this._tabBox.$status,
      statusMargins = graphics.margins($status),
      statusTop = top,
      statusPosition = this._tabBox.statusPosition,
      statusHeight = height - statusMargins.vertical();

    if (statusPosition === FormField.StatusPosition.DEFAULT) {
      statusTop += $header.cssMarginTop();
    } else {
      statusHeight -= $status.cssBorderWidthY(); // status has a transparent border to align icon with text
    }

    $status.cssWidth(this._statusWidth)
      .cssTop(statusTop)
      .cssRight(right)
      .cssHeight(statusHeight);
  }

  /**
   * Preferred size of the tab-box aligns every tab-item in a single line, so that each item is visible.
   */
  override preferredLayoutSize($container: JQuery, options: HtmlCompPrefSizeOptions): Dimension {
    options = options || {};
    let htmlContainer = HtmlComponent.get($container),
      htmlTabContent = HtmlComponent.get(this._tabBox._$tabContent),
      htmlHeader = HtmlComponent.get(this._tabBox.header.$container),
      headerSize = new Dimension(),
      tabContentSize = new Dimension(),
      $status = this._tabBox.$status,
      statusPosition = this._tabBox.statusPosition,
      headerWidthHint = htmlContainer.availableSize().subtract(htmlContainer.insets()).width;

    // HeightHint not supported
    options.heightHint = null;

    if (htmlHeader.isVisible()) {
      if ($status && $status.isVisible()) {
        if (statusPosition === FormField.StatusPosition.DEFAULT) {
          headerWidthHint -= $status.outerWidth(true);
        }
      }
      headerSize = htmlHeader.prefSize({
        widthHint: headerWidthHint
      })
        .add(htmlHeader.margins());
    }

    tabContentSize = htmlTabContent.prefSize(options)
      .add(htmlContainer.insets())
      .add(htmlTabContent.margins());

    return new Dimension(
      Math.max(headerSize.width, tabContentSize.width),
      tabContentSize.height + headerSize.height);
  }
}
