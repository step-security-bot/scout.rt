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
import {arrays, Event, EventHandler, FlexboxLayoutData, HtmlComponent, scout, SimpleTabArea, SimpleTabBoxController, SimpleTabBoxEventMap, SimpleTabBoxLayout, SimpleTabBoxModel, SimpleTabViewContentLayout, Widget} from '../index';
import {OutlineContent} from '../desktop/bench/DesktopBench';

export default class SimpleTabBox extends Widget implements SimpleTabBoxModel {
  declare model: SimpleTabBoxModel;
  declare eventMap: SimpleTabBoxEventMap;

  tabArea: SimpleTabArea;
  viewStack: OutlineContent[];
  currentView: OutlineContent;
  controller: SimpleTabBoxController;
  layoutData: FlexboxLayoutData;
  viewContent: HtmlComponent;
  $viewContent: JQuery<HTMLDivElement>;
  $tabArea: JQuery;

  protected _removeViewInProgress: number;
  protected _viewDestroyedHandler: EventHandler<Event<OutlineContent>>;

  constructor() {
    super();
    this._addWidgetProperties(['tabArea']);

    this.tabArea = null;
    this.viewStack = [];
    this.currentView = null;
    this.controller = null;
    this._removeViewInProgress = 0;
  }

  protected override _init(model: SimpleTabBoxModel) {
    super._init(model);
    this.cssClass = model.cssClass;

    if (!this.controller) {
      // default controller
      this.controller = scout.create(SimpleTabBoxController);
    }
    // link
    this.controller.install(this, this.tabArea);
    this.tabArea = this.controller.tabArea;

    this._viewDestroyedHandler = this._onViewDestroyed.bind(this);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv('simple-tab-box');
    if (this.cssClass) {
      this.$container.addClass(this.cssClass);
    }
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
    this.htmlComp.setLayout(new SimpleTabBoxLayout(this));
    this.htmlComp.layoutData = this.layoutData;

    // render content
    this.$viewContent = this.$container.appendDiv('tab-content');
    this.viewContent = HtmlComponent.install(this.$viewContent, this.session);
    this.viewContent.setLayout(new SimpleTabViewContentLayout(this));
  }

  protected override _renderProperties() {
    super._renderProperties();

    this._renderTabArea();
    this._renderView(this.currentView);
  }

  protected _renderTabArea() {
    this.tabArea.render();
    this.$tabArea = this.tabArea.$container;
    this.$tabArea.insertBefore(this.$viewContent);
  }

  protected _renderView(view: OutlineContent) {
    if (!view) {
      return;
    }
    if (view.rendered) {
      return;
    }
    view.render(this.$viewContent);
    view.$container.addClass('view');
    // @ts-ignore
    view.validateRoot = true; // FIXME TS: is this correct? should this be set on the HtmlComp instead?
  }

  postRender() {
    if (this.viewStack.length > 0 && !this.currentView) {
      this.activateView(this.viewStack[this.viewStack.length - 1]);
    }
  }

  activateView(view: OutlineContent) {
    if (view === this.currentView) {
      return;
    }

    if (this.currentView) {
      this.currentView.detach();
      this.trigger('viewDeactivate', {
        view: this.currentView
      });
      this.currentView = null;
    }
    // ensure rendered
    if (this.rendered) {
      this._renderView(view);
    }
    if (view && !view.attached) {
      view.attach();
    }

    this.currentView = view;

    this.trigger('viewActivate', {
      view: view
    });

    if (this.rendered) {
      this.viewContent.invalidateLayoutTree();
    }
  }

  override setLayoutData(layoutData: FlexboxLayoutData) {
    super.setLayoutData(layoutData);
    this.layoutData = layoutData;
  }

  getLayoutData(): FlexboxLayoutData {
    return this.layoutData;
  }

  /**
   * @param bringToTop whether the view should be placed on top of the view stack. the view tab will be selected. Default is true.
   */
  addView(view: OutlineContent, bringToTop?: boolean) {
    let activate = scout.nvl(bringToTop, true);
    // add to view stack
    let siblingView = this._addToViewStack(view, activate);
    // track focus when a view gets removed ond re-rendered.
    view.setTrackFocus(true);
    view.setParent(this);
    this.trigger('viewAdd', {
      view: view,
      siblingView: siblingView
    });

    if (activate) {
      this.activateView(view);
    }
  }

  /**
   * @returns the view which is gonna be the sibling to insert the new view tab after.
   */
  protected _addToViewStack(view: OutlineContent, bringToTop: boolean): OutlineContent {
    let sibling: OutlineContent;
    let index = this.viewStack.indexOf(view);
    if (index > -1) {
      return this.viewStack[index - 1];
    }

    if (!SimpleTabBoxController.hasViewTab(view)) {
      // first
      this.viewStack.unshift(view);
      this._addDestroyListener(view);
      return sibling;
    }
    if (!this.currentView || !bringToTop) {
      // end
      sibling = this.viewStack[this.viewStack.length - 1];
      this.viewStack.push(view);
      this._addDestroyListener(view);
      return sibling;
    }
    let currentIndex = this.viewStack.indexOf(this.currentView);
    sibling = this.viewStack[currentIndex];
    // it does not matter when index is -1 will be inserted at first position
    this.viewStack.splice(currentIndex + 1, 0, view);
    return sibling;
  }

  protected _addDestroyListener(view: OutlineContent) {
    view.one('destroy', this._viewDestroyedHandler);
  }

  protected _removeDestroyListener(view: OutlineContent) {
    view.off('destroy', this._viewDestroyedHandler);
  }

  protected _onViewDestroyed(event: Event<OutlineContent>) {
    let view = event.source;
    arrays.remove(this.viewStack, view);
    if (this.currentView === view) {
      if (this.rendered) {
        view.remove();
      }
      this.currentView = null;
    }
  }

  removeView(view: OutlineContent, showSiblingView?: boolean) {
    if (!view) {
      return;
    }
    // track focus when a view gets removed ond re-rendered.
    view.setTrackFocus(false);
    showSiblingView = scout.nvl(showSiblingView, true);
    let index = this.viewStack.indexOf(view);
    let viewToActivate;
    // if current view is the view to remove reset current view
    if (this.currentView === view) {
      this.currentView = null;
    } else {
      // Don't change selected view if not the selected view was removed
      showSiblingView = false;
    }

    if (index > -1) {
      // activate previous
      if (showSiblingView) {
        if (index - 1 >= 0) {
          viewToActivate = this.viewStack[index - 1];
        } else if (index + 1 < this.viewStack.length) {
          viewToActivate = this.viewStack[index + 1];
        }
      }

      // remove
      this.viewStack.splice(index, 1);
      if (view.rendered) {
        this._removeViewInProgress++;
        view.remove();
        this._removeViewInProgress--;
      }
      this.trigger('viewRemove', {
        view: view
      });

      if (this._removeViewInProgress === 0) {
        if (viewToActivate) {
          this.activateView(viewToActivate);
        }
        if (this.rendered) {
          this.viewContent.invalidateLayoutTree();
        }
      }
    }
  }

  getController(): SimpleTabBoxController {
    return this.controller;
  }

  viewCount(): number {
    return this.viewStack.length;
  }

  hasViews(): boolean {
    return this.viewStack.length > 0;
  }

  hasView(view: OutlineContent): boolean {
    return this.viewStack.filter(v => v === view).length > 0;
  }

  getViews(displayViewId?: string) {
    return this.viewStack.filter(view => {
      if (!displayViewId) {
        return true;
      }
      return displayViewId === view.displayViewId;
    });
  }
}
