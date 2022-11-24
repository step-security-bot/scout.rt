/*
 * Copyright (c) 2010-2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
import {NavigateUpButton} from '../../../../src/index';

describe('NavigateUpButton', () => {

  let session, outline, menu, node = {};

  beforeEach(() => {
    setFixtures(sandbox());
    session = sandboxSession();
    outline = {
      session: session,
      navigateToTop: () => {
      }
    };
    let model = createSimpleModel('NavigateUpButton', session);
    model.outline = outline;
    model.node = node;
    menu = new NavigateUpButton();
    menu.init(model);
  });

  it('_toggleDetail is always true', () => {
    expect(menu._toggleDetail()).toBe(true);
  });

  it('_isDetail returns true or false depending on the state of the detail-form and detail-table', () => {
    // false when both detailForm and detailTable are visible
    node.detailForm = {};
    node.detailFormVisible = true;
    node.detailFormVisibleByUi = true;
    node.detailTable = {};
    node.detailTableVisible = true;
    expect(menu._isDetail()).toBe(false);

    // false when detailForm is absent, even when if detailFormVisible=true
    delete node.detailForm;
    expect(menu._isDetail()).toBe(false);
    node.detailForm = {};

    // false when detailTable is absent, even when if detailTableVisible=true
    delete node.detailTable;
    expect(menu._isDetail()).toBe(false);
    node.detailTable = {};

    // true when detailForm is hidden by UI
    node.detailFormVisibleByUi = false;
    expect(menu._isDetail()).toBe(true);
    node.detailFormVisibleByUi = true;

    // false when property says to
    node.detailFormVisible = false;
    expect(menu._isDetail()).toBe(false);
    node.detailFormVisible = true;
    node.detailTableVisible = false;
    expect(menu._isDetail()).toBe(false);
  });

  describe('_buttonEnabled', () => {

    it('is true when current node has a parent or...', () => {
      node.parentNode = {};
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is true when current node is a top-level node and outline a default detail-form or...', () => {
      node.parentNode = undefined;
      outline.defaultDetailForm = {};
      expect(menu._buttonEnabled()).toBe(true);
    });

    it('is false otherwise', () => {
      node.parentNode = undefined;
      outline.defaultDetailForm = undefined;
      expect(menu._buttonEnabled()).toBe(false);
    });

  });

  describe('_drill', () => {

    beforeEach(() => {
      outline.selectNodes = node => {
      };
      outline.collapseNode = node => {
      };
      outline.collapseAll = node => {
      };
    });

    it('drills up to parent node, sets the selection on the tree', () => {
      node.parentNode = {};
      spyOn(outline, 'selectNodes');
      spyOn(outline, 'collapseNode');
      menu._drill();
      expect(outline.navigateUpInProgress).toBe(true);
      expect(outline.selectNodes).toHaveBeenCalledWith(node.parentNode);
      expect(outline.collapseNode).toHaveBeenCalledWith(node.parentNode, {collapseChildNodes: true});
    });

    it('shows default detail-form or outline overview', () => {
      node.parentNode = undefined;
      menu.drill;
      spyOn(outline, 'navigateToTop');
      menu._drill();
      expect(outline.navigateToTop).toHaveBeenCalled();
    });

  });

});
