  /*!
   * Eclipse Scout
   * https://eclipse.org/scout/
   *
   * Copyright (c) BSI Business Systems Integration AG. All rights reserved.
   * Released under the Eclipse Public License v1.0
   * http://www.eclipse.org/legal/epl-v10.html
   */
  // protects $ and undefined from being redefined by another library
  (function(scout, $, undefined) {
    __include("scout/desktop/outline/OutlineSpecHelper.js");
    __include("scout/form/FormSpecHelper.js");
    __include("scout/form/fields/beanfield/TestBeanField.js");
    __include("scout/form/fields/tabbox/TabBoxSpecHelper.js");
    __include("scout/form/fields/CloneSpecHelper.js");
    __include("scout/lookup/DummyLookupCall.js");
    __include("scout/lookup/AnotherDummyLookupCall.js");
    __include("scout/menu/MenuSpecHelper.js");
    __include("scout/table/TableSpecHelper.js");
    __include("scout/tree/TreeSpecHelper.js");
    __include("scout/text/LocaleSpecHelper.js");
  }(window.scout = window.scout || {}, jQuery)); // NOSONAR