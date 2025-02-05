/*
 * Copyright (c) 2010, 2023 BSI Business Systems Integration AG
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 */
import {App, AppBootstrapOptions, InitModelOf, LogoutAppModel, LogoutBox, ObjectFactory, scout, texts} from '../index';
import $ from 'jquery';

export class LogoutApp extends App {

  declare model: LogoutAppModel;

  protected override _prepareEssentials(options: LogoutAppModel) {
    ObjectFactory.get().init();
  }

  /**
   * No bootstrapping required
   */
  protected override _doBootstrap(options: AppBootstrapOptions): Array<JQuery.Promise<any>> {
    return [];
  }

  protected override _init(options: InitModelOf<this>): JQuery.Promise<any> {
    options = options || {} as InitModelOf<this>;
    options.texts = $.extend({}, texts.readFromDOM(), options.texts);
    this._prepareDOM();

    let logoutBox = scout.create(LogoutBox, options);
    logoutBox.render($('body').addClass('logout-body'));
    return $.resolvedPromise();
  }
}
