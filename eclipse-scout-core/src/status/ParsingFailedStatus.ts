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
import {Status} from '../index';
import {InitModelOf, ModelOf} from '../scout';

export default class ParsingFailedStatus extends Status {

  constructor(model?: InitModelOf<ParsingFailedStatus>) {
    super(model);
  }

  /**
   * @returns a {@link ParsingFailedStatus} object with severity ERROR.
   */
  static override error(model: ModelOf<Status> | string): ParsingFailedStatus {
    return new ParsingFailedStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }
}
