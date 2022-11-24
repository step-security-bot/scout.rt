/*
 * Copyright (c) 2010-2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {FormSpecHelper} from '../../../../src/testing/index';

describe('NumberFieldAdapter', () => {
  let session;
  let helper;

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    helper = new FormSpecHelper(session);
  });

  describe('parse', () => {
    let field;

    beforeEach(() => {
      field = helper.createField('NumberField');
      linkWidgetAndAdapter(field, 'NumberFieldAdapter');
    });

    it('prevents setting the error status', () => {
      // The parsing might fail on JS side, but the server might handle it -> don't show the error status, let the server do it
      // Example: property percentage is not known by the DecimalFormat.js yet, parse would fail.
      // But even if that will be implemented some time the developer may still override execParseValue and implement a custom parse logic in Java
      field.render();
      field.$field.val('asdf');
      field.acceptInput();
      expect(field.value).toBe(null);
      expect(field.displayText).toBe('asdf');
      expect(field.errorStatus).toBe(null);
    });

    it('prevents clearing the error status', () => {
      // The error status is handled completely by the server, thus it must not be cleared by JS, even if the value is valid
      // Use case: SequenceBox with number fields, second field contains smaller (invalid) value than first one, user types another too small value
      // -> error status must stay (server won't send another error status because the message has not changed)
      field.modelAdapter._syncPropertiesOnPropertyChange({
        errorStatus: {
          message: 'error status from server'
        }
      });
      field.render();
      expect(field.errorStatus.message).toBe('error status from server');

      field.$field.val(5);
      field.acceptInput();
      expect(field.errorStatus.message).toBe('error status from server');
    });

  });

  it('supports the calculator', () => {
    // Check if the calculator still works if a model adapter is attached
    let field = helper.createField('NumberField');
    linkWidgetAndAdapter(field, 'NumberFieldAdapter');
    field.render();
    field.decimalFormat.decimalSeparatorChar = '.';
    field.decimalFormat.groupingChar = '\'';

    field.$field.val('2.0+3.1');
    field.acceptInput();
    expect(field.$field[0].value).toBe('5.1');
  });

});
