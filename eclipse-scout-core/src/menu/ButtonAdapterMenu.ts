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
import {Action, Button, ButtonAdapterMenuModel, ButtonModel, Event, EventHandler, Menu, MenuBar, MenuModel, PropertyChangeEvent} from '../index';
import {ActionStyle, ActionTextPosition} from '../action/Action';
import {ButtonDisplayStyle} from '../form/fields/button/Button';
import {FormFieldLabelPosition} from '../form/fields/FormField';

export default class ButtonAdapterMenu extends Menu implements ButtonAdapterMenuModel {
  declare model: ButtonAdapterMenuModel;

  button: Button;
  menubar: MenuBar;

  protected _buttonPropertyChangeHandler: EventHandler<PropertyChangeEvent>;
  protected _buttonDestroyHandler: EventHandler<Event<Button>>;

  constructor() {
    super();
    this._removeWidgetProperties('childActions'); // managed by button

    this._buttonPropertyChangeHandler = this._onButtonPropertyChange.bind(this);
    this._buttonDestroyHandler = this._onButtonDestroy.bind(this);

    this._addCloneProperties(['button']);
    this.button = null;
    this.menubar = null;
  }

  protected override _init(model: ButtonAdapterMenuModel) {
    super._init(model);
    if (!this.button) {
      throw new Error('Cannot adapt to undefined button');
    }
    this.button.adaptedBy = this;
    this._installListeners();
  }

  protected override _destroy() {
    super._destroy();
    delete this.button.adaptedBy;
  }

  protected _installListeners() {
    this.button.on('propertyChange', this._buttonPropertyChangeHandler);
    this.button.on('destroy', this._buttonDestroyHandler);
  }

  protected _uninstallListeners() {
    this.button.off('propertyChange', this._buttonPropertyChangeHandler);
    this.button.off('destroy', this._buttonDestroyHandler);
  }

  protected override _render() {
    super._render();
    // Convenience: Add ID of original button to DOM for debugging purposes
    this.$container.attr('data-buttonadapter', this.button.id);
  }

  protected _onButtonPropertyChange(event: PropertyChangeEvent) {
    // Whenever a button property changes, apply the changes to the menu
    let changedProperties: Partial<ButtonModel> = {};
    changedProperties[event.propertyName] = event.newValue;
    changedProperties = ButtonAdapterMenu.adaptButtonProperties(changedProperties);
    for (let prop in changedProperties) { // NOSONAR
      // Set the property (don't use callSetter because this may delegate to the button)
      this.setProperty(prop, changedProperties[prop]);
    }
  }

  protected _onButtonDestroy(event: Event<Button>) {
    this.destroy();
    this._uninstallListeners();
  }

  override doAction(): boolean {
    if (this.childActions.length > 0) {
      // Popup menu is handled by this menu itself
      return super.doAction();
    }

    // Everything else is delegated to the button
    let actionExecuted = this.button.doAction();
    if (actionExecuted) {
      if (this.isToggleAction()) {
        this.setSelected(!this.selected);
      }
      this._doAction();
    }
    return actionExecuted;
  }

  override focus(): boolean {
    if (!this.rendered) {
      this.session.layoutValidator.schedulePostValidateFunction(this.focus.bind(this));
      return false;
    }
    this.menubar.setTabbableMenu(this);
    return this.session.focusManager.requestFocus(this.getFocusableElement());
  }

  static adaptButtonProperties(buttonProperties: Partial<ButtonModel>, menuProperties?: Partial<MenuModel>): Partial<MenuModel> {
    menuProperties = menuProperties || {};

    // Plain properties: simply copy, no translation required
    ['visible', 'selected', 'tooltipText', 'keyStroke', 'keyStrokes', 'cssClass', 'modelClass', 'classId', 'iconId', 'preventDoubleClick', 'enabled', 'inheritAccessibility', 'stackable', 'shrinkable'].forEach(prop => {
      menuProperties[prop] = buttonProperties[prop];
    });

    // Properties requiring special handling (non-trivial mapping)
    menuProperties.text = buttonProperties.label;
    menuProperties.textPosition = labelPositionToTextPosition(buttonProperties.labelPosition);
    menuProperties.horizontalAlignment = buttonProperties.gridData ? buttonProperties.gridData.horizontalAlignment : undefined;
    menuProperties.actionStyle = buttonStyleToActionStyle(buttonProperties.displayStyle);
    menuProperties.toggleAction = buttonProperties.displayStyle === Button.DisplayStyle.TOGGLE;
    menuProperties.childActions = buttonProperties.menus as Menu[];
    if (menuProperties.defaultMenu === undefined) {
      // buttonProperties.defaultButton property is only mapped if it is true, false should not be mapped as the default defaultMenu = null setting
      // would be overridden if this default null setting is overridden MenuBar.prototype.updateDefaultMenu would not consider these entries anymore
      // on actual property changes defaultMenu will always be undefined which always maps the defaultButton property to the defaultMenu property
      menuProperties.defaultMenu = buttonProperties.defaultButton;
    }

    // Cleanup: Remove all properties that have value 'undefined' from the result object,
    // otherwise, they would be applied to the model adapter.
    for (let prop in menuProperties) {
      if (menuProperties[prop] === undefined) {
        delete menuProperties[prop];
      }
    }
    return menuProperties;

    function buttonStyleToActionStyle(buttonStyle: ButtonDisplayStyle): ActionStyle {
      if (buttonStyle === undefined) {
        return undefined;
      }
      if (buttonStyle === Button.DisplayStyle.LINK) {
        return Action.ActionStyle.DEFAULT;
      }
      return Action.ActionStyle.BUTTON;
    }

    function labelPositionToTextPosition(labelPosition: FormFieldLabelPosition): ActionTextPosition {
      if (labelPosition === undefined) {
        return undefined;
      }
      if (labelPosition === Button.LabelPosition.BOTTOM) {
        return Action.TextPosition.BOTTOM;
      }
      return Action.TextPosition.DEFAULT;
    }
  }
}
