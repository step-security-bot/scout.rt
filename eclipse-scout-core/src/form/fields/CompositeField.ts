/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {EventHandler, fields, FormField, FormFieldStyle, InitModelOf, PropertyChangeEvent, TreeVisitResult, widgets} from '../../index';

export abstract class CompositeField extends FormField {
  protected _childFieldPropertyChangeHandler: EventHandler<PropertyChangeEvent>;

  protected override _init(model: InitModelOf<this>) {
    super._init(model);
    this._childFieldPropertyChangeHandler = this._onChildFieldPropertyChange.bind(this);
  }

  /**
   * @returns an array of child-fields.
   */
  abstract getFields(): FormField[];

  override visitFields(visitor: (field: FormField) => TreeVisitResult | void): TreeVisitResult | void {
    let treeVisitResult = super.visitFields(visitor);
    if (treeVisitResult === TreeVisitResult.TERMINATE) {
      return TreeVisitResult.TERMINATE;
    }
    if (treeVisitResult === TreeVisitResult.SKIP_SUBTREE) {
      return TreeVisitResult.CONTINUE;
    }

    let fields = this.getFields();
    for (let i = 0; i < fields.length; i++) {
      let field = fields[i];
      treeVisitResult = field.visitFields(visitor);
      if (treeVisitResult === TreeVisitResult.TERMINATE) {
        return TreeVisitResult.TERMINATE;
      }
    }
  }

  /**
   * Sets the given fieldStyle recursively on all fields of the composite field.
   */
  override setFieldStyle(fieldStyle: FormFieldStyle) {
    this.getFields().forEach(field => field.setFieldStyle(fieldStyle));
    super.setFieldStyle(fieldStyle);
  }

  override activate() {
    fields.activateFirstField(this, this.getFields());
  }

  override getFocusableElement(): HTMLElement | JQuery {
    let field = widgets.findFirstFocusableWidget(this.getFields(), this);
    if (field) {
      return field.getFocusableElement();
    }
    return null;
  }

  override computeRequiresSave(): boolean {
    let requiresSave = super.computeRequiresSave();
    if (requiresSave) {
      return true;
    }
    for (const field of this.getFields()) {
      if (field.requiresSave) {
        return true;
      }
    }
    return false;
  }

  protected _onChildFieldPropertyChange(event: PropertyChangeEvent<any, FormField>) {
    if (event.propertyName === 'requiresSave') {
      this.updateRequiresSave();
    }
  }
}
