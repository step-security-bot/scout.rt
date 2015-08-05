var MenuSpecHelper = function(session) {
  this.session = session;
};

MenuSpecHelper.prototype.createModel = function(text, iconId, menuTypes) {
  var model = createSimpleModel('Menu');
  $.extend(model, {
    text: text,
    iconId: iconId,
    menuTypes: menuTypes,
    visible: true
  });
  return model;
};

MenuSpecHelper.prototype.createMenu = function(model) {
  model.objectType = model.objectType || 'Menu';
  var offlineSession = scout.OfflineSession.createFromSession(this.session);
  return offlineSession.createUiObject(model);
};
