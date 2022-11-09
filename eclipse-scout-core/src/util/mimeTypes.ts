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
import {scout} from '../index';

const APPLICATION_JSON = 'application/json';
const APPLICATION_JAVASCRIPT = 'application/javascript';
const APPLICATION_ZIP = 'application/zip';

/**
 * Pendant for org.eclipse.scout.rt.platform.resource.MimeType enumeration.
 */
export const mimeTypes = {

  TEXT_PLAIN: 'text/plain',
  TEXT_HTML: 'text/html',
  TEXT_CSS: 'text/css',
  TEXT_JAVASCRIPT: 'text/javascript',

  IMAGE_PNG: 'image/png',
  IMAGE_JPG: 'image/jpg',
  IMAGE_JPEG: 'image/jpeg',
  IMAGE_GIF: 'image/gif',

  APPLICATION_JSON,
  APPLICATION_JAVASCRIPT,
  APPLICATION_ZIP,

  JSON: APPLICATION_JSON,
  JAVASCRIPT: APPLICATION_JAVASCRIPT,
  ZIP: APPLICATION_ZIP,

  isTextMimeType(mimeType: string): boolean {
    return scout.isOneOf(mimeType,
      mimeTypes.TEXT_PLAIN,
      mimeTypes.TEXT_HTML,
      mimeTypes.TEXT_CSS,
      mimeTypes.TEXT_JAVASCRIPT,
      mimeTypes.APPLICATION_JAVASCRIPT,
      mimeTypes.APPLICATION_JSON);
  },

  isJavaScript(mimeType: string): boolean {
    return scout.isOneOf(mimeType,
      mimeTypes.TEXT_JAVASCRIPT,
      mimeTypes.APPLICATION_JAVASCRIPT);
  }
};
