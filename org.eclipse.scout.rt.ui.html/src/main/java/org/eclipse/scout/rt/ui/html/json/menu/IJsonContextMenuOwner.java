/*******************************************************************************
 * Copyright (c) 2010 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.ui.html.json.menu;

import java.util.List;

import org.eclipse.scout.rt.ui.html.json.IJsonAdapter;

public interface IJsonContextMenuOwner {

  String PROP_MENUS = "menus";

  String PROP_MENUS_VISIBLE = "menusVisible";

  void handleModelContextMenuChanged(List<IJsonAdapter<?>> menuAdapters);
}
