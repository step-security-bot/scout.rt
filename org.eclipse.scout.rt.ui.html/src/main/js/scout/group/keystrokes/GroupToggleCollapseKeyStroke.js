/*******************************************************************************
 * Copyright (c) 2018 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
scout.GroupToggleCollapseKeyStroke = function(group) {
  scout.GroupToggleCollapseKeyStroke.parent.call(this, group);
  this.field = group;
  this.which = [scout.keys.SPACE];
};
scout.inherits(scout.GroupToggleCollapseKeyStroke, scout.KeyStroke);

scout.GroupToggleCollapseKeyStroke.prototype.handle = function(event) {
  this.field.toggleCollapse();
};