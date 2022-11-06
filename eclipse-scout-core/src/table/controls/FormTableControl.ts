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
import {Event, EventHandler, Form, FormTableControlEventMap, FormTableControlLayout, FormTableControlModel, GroupBox, TabBox, TableControl} from '../../index';
import AbstractLayout from '../../layout/AbstractLayout';
import {InitModelOf} from '../../scout';

export default class FormTableControl extends TableControl implements FormTableControlModel {
  declare model: FormTableControlModel;
  declare eventMap: FormTableControlEventMap;
  declare self: FormTableControl;

  form: Form;
  protected _formDestroyedHandler: EventHandler<Event<Form>>;

  constructor() {
    super();
    this._addWidgetProperties('form');
    this._formDestroyedHandler = this._onFormDestroyed.bind(this);
  }

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._setForm(this.form);
  }

  protected override _createLayout(): AbstractLayout {
    return new FormTableControlLayout(this);
  }

  protected override _renderContent($parent: JQuery) {
    this.form.renderInitialFocusEnabled = false;
    this.form.render($parent);

    // Tab box gets a special style if it is the first field in the root group box
    let rootGroupBox = this.form.rootGroupBox;
    if (rootGroupBox.controls[0] instanceof TabBox) {
      rootGroupBox.controls[0].$container.addClass('in-table-control');
    }

    this.form.$container.height($parent.height());
    this.form.$container.width($parent.width());
    this.form.htmlComp.validateRoot = true;
    this.form.htmlComp.validateLayout();
  }

  protected override _removeContent() {
    if (this.form) {
      this.form.remove();
    }
  }

  protected _removeForm() {
    this.removeContent();
  }

  protected _renderForm(form: Form) {
    this.renderContent();
  }

  /**
   * Returns true if the table control may be displayed (opened).
   */
  override isContentAvailable(): boolean {
    return !!this.form;
  }

  protected _setForm(form: Form) {
    if (this.form) {
      this.form.off('destroy', this._formDestroyedHandler);
    }
    if (form) {
      form.on('destroy', this._formDestroyedHandler);
      this._adaptForm(form);
    }
    this._setProperty('form', form);
  }

  protected _adaptForm(form: Form) {
    form.rootGroupBox.setMenuBarPosition(GroupBox.MenuBarPosition.BOTTOM);
    form.setDisplayHint(Form.DisplayHint.VIEW);
    form.setModal(false);
    form.setAskIfNeedSave(false);
    form.setClosable(false);
  }

  override onControlContainerOpened() {
    if (!this.form || !this.form.rendered) {
      return;
    }
    this.form.renderInitialFocus();
  }

  protected _onFormDestroyed(event: Event<Form>) {
    // Called when the inner form is destroyed --> unlink it from this table control
    this._removeForm();
    this._setForm(null);
  }
}
