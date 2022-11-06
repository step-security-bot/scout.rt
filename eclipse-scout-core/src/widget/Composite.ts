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
import {CompositeEventMap, CompositeModel, HtmlComponent, Widget} from '../index';
import {ObjectOrChildModel} from '../scout';

export default class Composite extends Widget implements CompositeModel {
  declare model: CompositeModel;
  declare eventMap: CompositeEventMap;
  declare self: Composite;
  widgets: Widget[];

  constructor() {
    super();

    this.widgets = [];
    this._addWidgetProperties(['widgets']);
  }

  protected override _render() {
    this.$container = this.$parent.appendDiv();
    this.htmlComp = HtmlComponent.install(this.$container, this.session);
  }

  protected override _renderProperties() {
    super._renderProperties();
    this._renderWidgets();
  }

  setWidgets(widgets: ObjectOrChildModel<Widget>[]) {
    this.setProperty('widgets', widgets);
  }

  protected _renderWidgets() {
    this.widgets.forEach(widget => widget.render());
    this.invalidateLayoutTree();
  }
}
