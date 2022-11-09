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

// noinspection SpellCheckingInspection
import jscodeshift from 'jscodeshift';
import {defaultRecastOptions, findParentPath} from './common.js';

const j = jscodeshift.withParser('ts');

/**
 * @type import('ts-migrate-server').Plugin<{paramTypeMap?: object, moduleMap?: object, defaultParamType?: string}>
 */
const widgetColumnMapPlugin = {
  name: 'widget-column-map-plugin',

  async run({text, fileName, options}) {
    let className = fileName.substring(fileName.lastIndexOf('/') + 1, fileName.lastIndexOf('.'));

    if (!className || !className.endsWith('Model')) {
      return text;
    }

    let root = j(text);
    let widgets = new Map(),
      tables = new Map();

    // parse model and find all objects containing an id and objectType property
    // noinspection JSCheckFunctionSignatures
    root.find(j.ExportDefaultDeclaration)
      .find(j.ArrowFunctionExpression)
      .find(j.ObjectExpression, createObjectPropertyFilter('id', 'objectType'))
      .forEach(path => {
        let node = path.node,
          idAndObjectType = getIdAndObjectType(node),
          objectType = idAndObjectType.objectType;
        if (isWidget(objectType)) {
          // remember id and objectType of all widget nodes
          widgets.set(node, idAndObjectType);
        }
        if (isColumn(objectType)) {
          // collect all column infos for one table
          let tablePath = findParentTablePath(path),
            tableInfo = tables.get(tablePath.node);
          if (!tableInfo) {
            let tableFieldPath = findParentTableFieldPath(tablePath);
            tableInfo = {
              // create a table class name from the id of the table or the id of the tableField
              tableClassName: createTableClassName(getId(tablePath.node), getId(tableFieldPath.node)),
              columns: new Map()
            };
            tables.set(tablePath.node, tableInfo);
          }
          // remember id and objectType of all column nodes
          let columns = tableInfo.columns;
          columns.set(node, idAndObjectType);
        }
      });

    if (widgets.size > 0) { // only check size of widgets, if there are entries in tables then there are entries in widgets
      let body = root.get().node.program.body;

      // get/create widgetMap
      let widgetName = className.substring(0, className.lastIndexOf('Model')),
        widgetMapName = `${widgetName}WidgetMap`,
        widgetMapType = getOrCreateExportedType(widgetMapName, root, body),
        widgetMapMembers = widgetMapType.typeAnnotation.members,
        widgetMapProperties = [];

      // create a property for every entry of widgets
      widgets.forEach(({id, objectType}, node) => {
        let tableInfo = tables.get(node);
        if (tableInfo) {
          // add specific table class if available, will be created later on
          objectType = tableInfo.tableClassName;
        }
        widgetMapProperties.push(createMapProperty(id, objectType));
      });

      // set/replace properties of widgetMap
      widgetMapMembers.splice(0, widgetMapMembers.length, ...widgetMapProperties);

      // create table class and columnMap for every table
      tables.forEach((tableInfo, node) => {
        // get/create columnMap
        let columnMapName = `${tableInfo.tableClassName}ColumnMap`,
          columnMapType = getOrCreateExportedType(columnMapName, root, body),
          columnMapMembers = columnMapType.typeAnnotation.members,
          columnMapProperties = [];

        // create a property for every entry of tableInfo.columns
        tableInfo.columns.forEach(({id, objectType}) => {
          columnMapProperties.push(createMapProperty(id, objectType));
        });

        // set/replace properties of columnMap
        columnMapMembers.splice(0, columnMapMembers.length, ...columnMapProperties);

        // get/create tableClass
        let tableClassName = tableInfo.tableClassName,
          tableClass = getOrCreateExportedClass(tableClassName, root, body),
          tableMembers = tableClass.body.body,
          tableSuperClass = widgets.get(node).objectType,
          columnMapProperty = createClassProperty('columnMap', columnMapName);

        // set superClass to objectType from model
        tableClass.superClass = j.identifier(tableSuperClass);

        // declare columnMap property
        columnMapProperty.declare = true;
        tableMembers.splice(0, tableMembers.length, columnMapProperty);
      });
    }

    return root.toSource(defaultRecastOptions);
  }
};

function createObjectPropertyFilter(...propertyNames) {
  return {properties: propertyNames.map(name => ({type: 'ObjectProperty', key: {name: name}}))};
}

function findObjectProperty(objectNode, propertyName) {
  return objectNode.properties.find(
    n =>
      n.type === 'ObjectProperty' &&
      n.key.type === 'Identifier' &&
      n.key.name === propertyName
  );
}

function getId(node) {
  let idProperty = findObjectProperty(node, 'id');
  return idProperty.value.value;
}

function getObjectType(node) {
  let objectTypeProperty = findObjectProperty(node, 'objectType');
  return objectTypeProperty.value.name;
}

function getIdAndObjectType(node) {
  let id = getId(node),
    objectType = getObjectType(node);
  return {id, objectType};
}

function findParentTablePath(columnPath) {
  return findParentPathByObjectType(columnPath, isTable);
}

function findParentTableFieldPath(tablePath) {
  return findParentPathByObjectType(tablePath, isTableField);
}

function findParentPathByObjectType(path, objectTypePredicate) {
  return findParentPath(path, p => p.node.type === 'ObjectExpression' && objectTypePredicate((findObjectProperty(p.node, 'objectType') || {value: {}}).value.name));
}

function createTableClassName(tableId, tableFieldId) {
  if (tableId && tableId !== 'Table') {
    return tableId.replaceAll('.', '');
  }
  if (tableFieldId) {
    return tableFieldId.replaceAll('.', '') + 'Table';
  }
  throw new Error('At least one of tableId, tableFieldId must be set');
}

function getOrCreateExportedType(name, root, body) {
  let candidates = root
    .find(j.TSTypeAliasDeclaration)
    .filter(/** NodePath<TSTypeAliasDeclaration, TSTypeAliasDeclaration> */path => path.node.id.name === name);
  if (candidates.length) {
    return candidates.get().node;
  }
  let type = j.tsTypeAliasDeclaration(j.identifier(name), j.tsTypeLiteral([]));
  body.push(j.exportNamedDeclaration(type));
  return type;
}

function getOrCreateExportedClass(name, root, body) {
  let candidates = root
    .find(j.ClassDeclaration)
    .filter(/** NodePath<ClassDeclaration, ClassDeclaration> */path => path.node.id.name === name);
  if (candidates.length) {
    return candidates.get().node;
  }
  let type = j.classDeclaration(j.identifier(name), j.classBody([]), null);
  body.push(j.exportNamedDeclaration(type));
  return type;
}

function createMapProperty(id, objectType) {
  let identifier = j.identifier(`'${id}'`),
    // add trailing ; to type, otherwise there is no ; at the end of the line when you use tsPropertySignature
    typeAnnotation = j.tsTypeAnnotation(j.tsTypeReference(j.identifier(`${objectType};`)));
  return j.tsPropertySignature(identifier, typeAnnotation);
}

function createClassProperty(id, objectType) {
  let identifier = j.identifier(id),
    typeAnnotation = j.tsTypeAnnotation(j.tsTypeReference(j.identifier(objectType)));
  return j.classProperty(identifier, null, typeAnnotation);
}

function isWidget(objectType) {
  return objectType && !isColumn(objectType)
    && !objectType.endsWith('TableRow')
    && !objectType.endsWith('TreeNode')
    && !objectType.endsWith('Page')
    && objectType !== 'Status'
    && !objectType.endsWith('CodeType')
    && !objectType.endsWith('LookupCall');
}

function isColumn(objectType) {
  return objectType && objectType.endsWith('Column');
}

function isTable(objectType) {
  return objectType && objectType.endsWith('Table');
}

function isTableField(objectType) {
  return objectType && objectType.endsWith('TableField');
}

export default widgetColumnMapPlugin;
