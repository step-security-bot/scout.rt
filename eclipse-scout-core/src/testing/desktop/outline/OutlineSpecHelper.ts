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
import {ModelAdapter, Outline, OutlineAdapter, Page, Session} from '../../../index';
import {FormSpecHelper, TableSpecHelper} from '../../index';
import $ from 'jquery';
import {FullModelOf, InitModelOf, ModelOf, ObjectOrModel} from '../../../scout';

export default class OutlineSpecHelper {
  session: Session;

  constructor(session: Session) {
    this.session = session;
  }

  createModelFixture(nodeCount?: number, depth?: number, expanded?: boolean): FullModelOf<Outline> & { id: string; session: Session } {
    return this.createModel(this.createModelNodes(nodeCount, depth, expanded));
  }

  createModel(nodes: ObjectOrModel<Page>[]): FullModelOf<Outline> & { id: string; session: Session } {
    let model = createSimpleModel('Outline', this.session) as FullModelOf<Outline> & { id: string; session: Session };
    if (nodes) {
      model.nodes = nodes;
    }
    return model;
  }

  createModelNode(id: string, text: string): ModelOf<Page> {
    return {
      id: id,
      text: text
    };
  }

  createModelNodes(nodeCount: number, depth?: number, expanded?: boolean): ModelOf<Page>[] {
    return this.createModelNodesInternal(nodeCount, depth, expanded);
  }

  createModelNodesInternal(nodeCount: number, depth?: number, expanded?: boolean, parentNode?: ModelOf<Page>): ModelOf<Page>[] {
    if (!nodeCount) {
      return;
    }

    let nodes: ModelOf<Page>[] = [], nodeId;
    if (!depth) {
      depth = 0;
    }
    for (let i = 0; i < nodeCount; i++) {
      nodeId = i;
      if (parentNode) {
        nodeId = parentNode.id + '_' + nodeId;
      }
      nodes[i] = this.createModelNode(nodeId, 'node ' + i);
      nodes[i].expanded = expanded;
      if (depth > 0) {
        nodes[i].childNodes = this.createModelNodesInternal(nodeCount, depth - 1, expanded, nodes[i]);
      }
    }
    return nodes;
  }

  createOutline(model?: ModelOf<Outline>): Outline {
    let defaults = {
      parent: this.session.desktop
    };
    let m = $.extend({}, defaults, model) as InitModelOf<Outline>;
    let outline = new Outline();
    outline.init(m);
    return outline;
  }

  createOutlineAdapter(model: InitModelOf<ModelAdapter> | ModelOf<Outline> & { id: string; session: Session }): OutlineAdapter {
    let outlineAdapter = new OutlineAdapter();
    outlineAdapter.init(model as InitModelOf<OutlineAdapter>);
    return outlineAdapter;
  }

  /**
   * Creates an outline with 3 nodes, the first node has a visible detail form
   */
  createOutlineWithOneDetailForm(): Outline {
    let model = this.createModelFixture(3, 2, true);
    let outline = this.createOutline(model);
    let node = outline.nodes[0];
    node.detailForm = new FormSpecHelper(this.session).createFormWithOneField({
      modal: false
    });
    node.detailFormVisible = true;
    return outline;
  }

  /**
   * Creates an outline with 3 nodes, the first node has a visible detail table
   */
  createOutlineWithOneDetailTable(): Outline {
    let model = this.createModelFixture(3, 2, true);
    let outline = this.createOutline(model);
    let node = outline.nodes[0];
    node.detailTable = new TableSpecHelper(this.session).createTableWithOneColumn();
    node.detailTableVisible = true;
    return outline;
  }

  setMobileFlags(outline: Outline) {
    outline.setBreadcrumbStyleActive(true);
    outline.setToggleBreadcrumbStyleEnabled(false);
    outline.setCompact(true);
    outline.setEmbedDetailContent(true);
  }
}
