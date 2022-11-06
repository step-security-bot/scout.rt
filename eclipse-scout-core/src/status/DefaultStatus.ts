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

/**
 * The DefaultStatus class is used add programmatic Status triggered by business logic
 * in cases where you don't want or don't have to implement your own Status sub-class.
 */
export default class DefaultStatus extends Status {

  constructor(model?: InitModelOf<DefaultStatus>) {
    super(model);
  }

  /**
   * @returns a {@link DefaultStatus} object with severity ERROR.
   */
  static override error(model: ModelOf<Status> | string): DefaultStatus {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.ERROR));
  }

  /**
   * @returns a {@link DefaultStatus} object with severity WARNING.
   */
  static override warning(model: ModelOf<Status> | string): DefaultStatus {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.WARNING));
  }

  /**
   * @returns a {@link DefaultStatus} object with severity INFO.
   */
  static override info(model: ModelOf<Status> | string): DefaultStatus {
    return new DefaultStatus(Status.ensureModel(model, Status.Severity.INFO));
  }
}
