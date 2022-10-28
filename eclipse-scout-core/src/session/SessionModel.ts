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
import {Locale, UserAgent} from '../index';
import {AjaxCallModel} from '../ajax/AjaxCall';
import LocaleModel from './LocaleModel';

export default interface SessionModel {
  /**
   * The HTML element that is used by the {@link Desktop} to render its content.
   */
  $entryPoint: JQuery;

  /**
   * Necessary when multiple UI sessions are managed by the same window (portlet support).
   * Each session's partId must be unique. Default is 0.
   */
  portletPartId?: string;

  /**
   * Identifies the 'client instance' on the UI server. If the property is not set (which is the default case), the clientSessionId is taken from the browser's session storage (per browser window, survives F5 refresh of page).
   * If no clientSessionId can be found, a new one is generated on the server.
   */
  clientSessionId?: string;

  /**
   * If set to true, the stored or passed clientSessionId will be ignored and a new one generated by the server.
   */
  forceNewClientSession?: boolean;

  /**
   * By default, the user agent for the running platform is used. Use this option if you want to set a custom one.
   */
  userAgent?: UserAgent;

  /**
   * If not specified, {@link Locale.DEFAULT} is used.
   */
  locale?: Locale | LocaleModel;

  /**
   * Unless websockets is used, this property turns on (default) or off background polling using an async ajax call together with setTimeout()
   */
  backgroundJobPollingEnabled?: boolean;

  /**
   * Basically added because of Jasmine-tests.
   * When working with async tests that use setTimeout(), sometimes the Jasmine-Maven plug-in fails and aborts the build because there were console errors.
   * These errors always happen in this class. That's why we can skip suppress error handling with this flag.
   */
  suppressErrors?: boolean;

  /**
   * Forces the focus manager to be active or not. If undefined, the value is auto detected by {@link Device}.
   */
  focusManagerActive?: boolean;

  /**
   * Properties of this object are copied to the Session's reconnector instance (see {@link Reconnector}).
   */
  reconnectorOptions?: Record<string, any>;

  /**
   * Properties of this object are copied to all instances of {@link AjaxCall}.
   */
  ajaxCallOptions?: AjaxCallModel;
}
