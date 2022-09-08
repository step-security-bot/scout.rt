interface EventBase {
  source: object;
}

interface ClickEvent extends EventBase {
  data: number;
}

interface ButtonEventMap {
  'click': ClickEvent;
}

class Button {
  declare eventMap: ButtonEventMap;

  trigger<K extends keyof this['eventMap']>(type: K, event: Omit<this['eventMap'][K], 'source'>) {
    // ...
  }

  click() {
    this.trigger('click', { // <-- Error
      data: 1
    });
  }
}
