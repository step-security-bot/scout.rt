/*
 * Copyright (c) 2014-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {EventHandler, GroupBoxMenuItemsOrder, HtmlComponent, MenuBar, PropertyChangeEvent, scout, TabArea, TabBoxHeaderLayout, TabItem, Widget, WidgetEventMap, WidgetModel} from '../../../index';
import TabBox from './TabBox';
import Tab from './Tab';

export interface TabBoxHeaderModel extends WidgetModel {
  tabBox: TabBox;
}

export interface TabBoxHeaderEventMap extends WidgetEventMap {
  'propertyChange:selectedTabItem': PropertyChangeEvent<TabItem>;
}

export default class TabBoxHeader extends Widget implements TabBoxHeaderModel {
  declare model: TabBoxHeaderModel;
  declare eventMap: TabBoxHeaderEventMap;
  declare self: TabBoxHeader;

  tabBox: TabBox;
  tabArea: TabArea;
  menuBar: MenuBar;
  $borderBottom: JQuery;
  protected _tabBoxPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _tabAreaPropertyChangeHandler: EventHandler<PropertyChangeEvent>;

  constructor() {
    super();

    this.tabBox = null;
    this.tabArea = null;
    this.menuBar = null;
    this.$borderBottom = null;
    this._tabBoxPropertyChangeHandler = this._onTabBoxPropertyChange.bind(this);
    this._tabAreaPropertyChangeHandler = this._onTabAreaPropertyChange.bind(this);
  }

  protected override _init(options: TabBoxHeaderModel) {
    super._init(options);

    this.tabArea = scout.create(TabArea, {
      parent: this,
      tabBox: this.tabBox
    });
    this.tabArea.on('propertyChange', this._tabAreaPropertyChangeHandler);

    this.menuBar = scout.create(MenuBar, {
      parent: this,
      menuOrder: new GroupBoxMenuItemsOrder()
    });

    this.tabBox.on('propertyChange', this._tabBoxPropertyChangeHandler);
    this.menuBar.setMenuItems(this.tabBox.menus);
    this.setVisible(this.tabBox.labelVisible);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('tab-box-header');
    this.$borderBottom = this.$container.appendDiv('bottom-border');
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new TabBoxHeaderLayout(this));
    this.tabArea.render(this.$container);
    this.menuBar.render(this.$container);
    this.$container.append(this.menuBar.$container);
  }

  protected override _destroy() {
    this.tabBox.off('propertyChange', this._tabBoxPropertyChangeHandler);
    this.tabArea.off('propertyChange', this._tabAreaPropertyChangeHandler);
    super._destroy();
  }

  setTabItems(tabItems: TabItem[]) {
    this.tabArea.setTabItems(tabItems);
  }

  protected _setSelectedTab(tab: Tab) {
    if (tab) {
      this.setSelectedTabItem(tab.tabItem);
    } else {
      this.setSelectedTabItem(null);
    }
  }

  setSelectedTabItem(tabItem: TabItem) {
    this.setProperty('selectedTabItem', tabItem);
  }

  protected _setSelectedTabItem(tabItem: TabItem) {
    this._setProperty('selectedTabItem', tabItem);
    this.tabArea.setSelectedTabItem(tabItem);
  }

  isTabItemFocused(tabItem: TabItem): boolean {
    return this.tabArea.isTabItemFocused(tabItem);
  }

  focusTabItem(tabItem: TabItem): boolean {
    return this.tabArea.focusTabItem(tabItem);
  }

  getTabForItem(tabItem: TabItem): Tab {
    return this.tabArea.getTabForItem(tabItem);
  }

  protected _onTabBoxPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'menus') {
      this.menuBar.setMenuItems(this.tabBox.menus);
    } else if (event.propertyName === 'labelVisible') {
      this.setVisible(this.tabBox.labelVisible);
    }
  }

  protected _onTabAreaPropertyChange(event: PropertyChangeEvent) {
    if (event.propertyName === 'selectedTab') {
      this._setSelectedTab(event.newValue);
    }
  }
}
