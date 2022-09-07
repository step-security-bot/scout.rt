import {DisabledStyle, LogicalGrid, PropertyChangeEvent, Widget} from '../index';
import {GlassPaneContribution} from './Widget';
import PropertyEventMap from '../events/PropertyEventMap';
import {EmptyObject} from '../types';

export interface HierarchyChangeEvent {
  oldParent: Widget;
  parent: Widget;
}

export interface GlassPaneContributionEvent {
  contribution: GlassPaneContribution;
}

export default interface WidgetEventMap extends PropertyEventMap {
  'init': EmptyObject;
  'destroy': EmptyObject;
  'render': EmptyObject;
  'remove': EmptyObject;
  'removing': EmptyObject;
  'glassPaneContributionAdded': GlassPaneContributionEvent;
  'glassPaneContributionRemoved': GlassPaneContributionEvent;
  'hierarchyChange': HierarchyChangeEvent;
  'propertyChange:enabled': PropertyChangeEvent<boolean>;
  'propertyChange:enabledComputed': PropertyChangeEvent<boolean>;
  'propertyChange:trackFocus': PropertyChangeEvent<boolean>;
  'propertyChange:scrollTop': PropertyChangeEvent<number>;
  'propertyChange:scrollLeft': PropertyChangeEvent<number>;
  'propertyChange:inheritAccessibility': PropertyChangeEvent<boolean>;
  'propertyChange:disabledStyle': PropertyChangeEvent<DisabledStyle>;
  'propertyChange:visible': PropertyChangeEvent<boolean>;
  'propertyChange:focused': PropertyChangeEvent<boolean>;
  'propertyChange:cssClass': PropertyChangeEvent<string>;
  'propertyChange:loading': PropertyChangeEvent<boolean>;
  'propertyChange:logicalGrid': PropertyChangeEvent<LogicalGrid>;
}
