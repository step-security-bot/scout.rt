import {Event, EventEmitter, PropertyEventEmitter} from '../index';

export default interface PropertyChangeEvent<PROP_TYPE> {
  propertyName: string;
  newValue: PROP_TYPE;
  oldValue: PROP_TYPE;
}
