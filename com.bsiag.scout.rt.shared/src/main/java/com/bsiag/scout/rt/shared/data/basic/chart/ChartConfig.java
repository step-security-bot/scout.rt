/*
 * Copyright (c) 2020 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the BSI CRM Software License v1.0
 * which accompanies this distribution as bsi-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package com.bsiag.scout.rt.shared.data.basic.chart;

import static org.eclipse.scout.rt.platform.util.StringUtility.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.scout.rt.platform.BEANS;
import org.eclipse.scout.rt.platform.util.CollectionUtility;
import org.eclipse.scout.rt.platform.util.ImmutablePair;
import org.eclipse.scout.rt.platform.util.Pair;
import org.eclipse.scout.rt.platform.util.TypeCastUtility;

public class ChartConfig implements IChartConfig {

  private static final long serialVersionUID = 1L;

  protected static final String DELIMITER = ".";
  protected static final String ESCAPED_DELIMITER = escapeRegexMetachars(DELIMITER);
  protected static final String ARRAY_INDEX = arrayIndex("\\d+", true);
  protected static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile(ARRAY_INDEX);
  protected static final Pattern LIST_EXPRESSION_PATTERN = Pattern.compile("\\w+" + ARRAY_INDEX);
  protected static final Pattern PROPERTY_PATTERN = Pattern.compile("\\w+(" + ARRAY_INDEX + ")?(" + ESCAPED_DELIMITER + "\\w+(" + ARRAY_INDEX + ")?)*");

  protected static final String TYPE = "type";
  protected static final String OPTIONS = "options";
  protected static final String AUTO_COLOR = combine(OPTIONS, "autoColor");
  protected static final String MAX_SEGMENTS = combine(OPTIONS, "maxSegments");
  protected static final String CLICKABLE = combine(OPTIONS, "clickable");
  protected static final String ANIMATION = combine(OPTIONS, "animation");
  protected static final String ANIMATION_DURATION = combine(ANIMATION, "duration");
  protected static final String TOOLTIPS = combine(OPTIONS, "tooltips");
  protected static final String TOOLTIPS_ENABLED = combine(TOOLTIPS, "enabled");
  protected static final String LEGEND = combine(OPTIONS, "legend");
  protected static final String LEGEND_DISPLAY = combine(LEGEND, "display");
  protected static final String LEGEND_CLICKABLE = combine(LEGEND, "clickable");
  protected static final String LEGEND_POSITION = combine(LEGEND, "position");
  protected static final String SCALES = combine(OPTIONS, "scales");
  protected static final String X_AXES = combine(SCALES, "xAxes");
  protected static final String Y_AXES = combine(SCALES, "yAxes");
  protected static final String SCALE_LABEL = "scaleLabel";
  protected static final String SCALE_LABEL_DISPLAY = combine(SCALE_LABEL, "display");
  protected static final String SCALE_LABEL_LABEL = combine(SCALE_LABEL, "labelString");

  private final Map<String, Object> m_properties = new HashMap<>();

  protected static String combine(String... elements) {
    return join(DELIMITER, elements);
  }

  protected static String arrayIndex(int index) {
    return arrayIndex(String.valueOf(index));
  }

  protected static String arrayIndex(String index) {
    return arrayIndex(index, false);
  }

  protected static String arrayIndex(String index, boolean regex) {
    return box(regex ? escapeRegexMetachars("[") : "[", index, regex ? escapeRegexMetachars("]") : "]");
  }

  @Override
  public ChartConfig copy() {
    ChartConfig chartConfig = BEANS.get(ChartConfig.class);
    chartConfig.m_properties.putAll(m_properties);
    return chartConfig;
  }

  /**
   * Support for different modifying operations of the {@link #m_properties} map. The different {@link ModifyMode}s
   * support validation of a given name-value-pair and performing their specific operation.
   */
  protected enum ModifyMode {
    PUT {
      @Override
      protected boolean validate(String name, Object value) {
        return super.validate(name, value) && value != null;
      }

      @Override
      protected boolean isCreateMissing() {
        return true;
      }

      @Override
      protected Object modify(Map<String, Object> map, String name, Object value) {
        if (map == null) {
          throw new IllegalArgumentException("The given Map is null.");
        }
        return map.put(name, value);
      }
    },
    REMOVE {
      @Override
      protected Object modify(Map<String, Object> map, String name, Object value) {
        if (CollectionUtility.isEmpty(map)) {
          return null;
        }
        Pair<String, Integer> nameIndexPair = ChartConfig.splitListExpression(name);
        if (nameIndexPair != null) {
          Object obj = map.get(nameIndexPair.getLeft());
          if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> l = (List<Map<String, Object>>) obj;
            if (nameIndexPair.getRight() >= 0 && nameIndexPair.getRight() < l.size()) {
              Map<String, Object> element = CollectionUtility.getElement(l, nameIndexPair.getRight());
              l.add(nameIndexPair.getRight(), new HashMap<>());
              return element;
            }
          }
        }
        else {
          return map.remove(name);
        }
        return null;
      }
    },
    GET {
      @Override
      protected Object modify(Map<String, Object> map, String name, Object value) {
        if (CollectionUtility.isEmpty(map)) {
          return null;
        }
        Pair<String, Integer> nameIndexPair = ChartConfig.splitListExpression(name);
        if (nameIndexPair != null) {
          Object obj = map.get(nameIndexPair.getLeft());
          if (obj instanceof List) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> l = (List<Map<String, Object>>) obj;
            return CollectionUtility.getElement(l, nameIndexPair.getRight());
          }
        }
        else {
          return map.get(name);
        }
        return null;
      }
    };

    protected boolean validate(String name, Object value) {
      return PROPERTY_PATTERN.matcher(name).matches();
    }

    protected boolean isCreateMissing() {
      return false;
    }

    protected abstract Object modify(Map<String, Object> map, String name, Object value);
  }

  @Override
  public IChartConfig withProperty(String name, Object value) {
    modifyProperties(ModifyMode.PUT, name, value);
    return this;
  }

  @Override
  public IChartConfig removeProperty(String name) {
    modifyProperties(ModifyMode.REMOVE, name, null);
    return this;
  }

  @Override
  public Object getProperty(String name) {
    return modifyProperties(ModifyMode.GET, name, null);
  }

  protected IChartConfig withArrayProperty(String array, int index, String name, Object value) {
    return withProperty(combine(array + arrayIndex(index), name), value);
  }

  protected IChartConfig removeArrayProperty(String array, int index, String name) {
    return removeProperty(combine(array + arrayIndex(index), name));
  }

  protected Object getArrayProperty(String array, int index, String name) {
    return getProperty(combine(array + arrayIndex(index), name));
  }

  /**
   * Modifies the {@link #m_properties} map depending on the given {@link ModifyMode}.
   *
   * @param modifyMode
   *          Specific {@link ModifyMode} like put or remove.
   * @param name
   *          The name of the property. Recursive properties have to be separated by "." and indices of arrays have to
   *          be specified in the form "[i]" (e.g. "options.legend.position", "options.scales.xAxes[0].scaleLabel").
   * @param value
   *          The value of the property or null for {@link ModifyMode}s that don't need a value.
   * @return The Object returned by the specific operation.
   */
  protected Object modifyProperties(ModifyMode modifyMode, String name, Object value) {
    if (modifyMode == null || !modifyMode.validate(name, value)) {
      throw new IllegalArgumentException("The given ModifyMode is null or validating it failed.");
    }

    List<String> namesRec = CollectionUtility.arrayList(name.split(ESCAPED_DELIMITER));
    String propertyName = CollectionUtility.lastElement(namesRec);
    Map<String, Object> map = m_properties;
    if (namesRec.size() > 1) {
      AtomicReference<Map<String, Object>> atomicMap = new AtomicReference<>(map);
      //noinspection ResultOfMethodCallIgnored
      CollectionUtility.slice(namesRec, 0, -2).stream().filter(nameRec -> {
        Pair<String, Integer> nameIndexPair = splitListExpression(nameRec);
        if (nameIndexPair != null) {
          nameRec = nameIndexPair.getLeft();
        }
        Object obj = atomicMap.get().get(nameRec);
        if (obj == null && modifyMode.isCreateMissing()) {
          Map<String, Object> m;
          if (nameIndexPair != null) {
            List<Map<String, Object>> l = Stream.generate(HashMap<String, Object>::new)
                .limit(nameIndexPair.getRight() + 1)
                .collect(Collectors.toList());
            atomicMap.get().put(nameRec, l);
            m = l.get(nameIndexPair.getRight());
          }
          else {
            m = new HashMap<>();
            atomicMap.get().put(nameRec, m);
          }
          atomicMap.set(m);
        }
        else if (nameIndexPair == null && obj instanceof Map) {
          @SuppressWarnings("unchecked")
          Map<String, Object> m = (Map<String, Object>) obj;
          atomicMap.set(m);
        }
        else if (nameIndexPair != null && obj instanceof List) {
          @SuppressWarnings("unchecked")
          List<Map<String, Object>> l = (List<Map<String, Object>>) obj;
          if (l.size() < nameIndexPair.getRight() + 1) {
            l.addAll(Stream.generate(HashMap<String, Object>::new)
                .limit(nameIndexPair.getRight() + 1 - l.size())
                .collect(Collectors.toList()));
          }
          atomicMap.set(l.get(nameIndexPair.getRight()));
        }
        else {
          atomicMap.set(null);
          return true;
        }
        return false;
      }).findFirst();
      map = atomicMap.get();
    }
    return modifyMode.modify(map, propertyName, value);
  }

  /**
   * Splits a given list expression into the name of the list and the index (e.g. "list[3]" -> "list", 3).
   *
   * @param listExpression
   *          The list expression in the form name of the list followed by the index in square brackets.
   * @return A {@link Pair} containing the name of the list as left and the index as right value or {@code null} if the
   *         given listExpression does not match the required form.
   */
  protected static Pair<String, Integer> splitListExpression(String listExpression) {
    Matcher arrayIndexMatcher = ARRAY_INDEX_PATTERN.matcher(listExpression);
    if (LIST_EXPRESSION_PATTERN.matcher(listExpression).matches() && arrayIndexMatcher.find()) {
      int start = arrayIndexMatcher.start();
      String indexString = listExpression.substring(start).replaceAll("\\D", "");
      return new ImmutablePair<>(listExpression.substring(0, start), TypeCastUtility.castValue(indexString, Integer.class));
    }
    return null;
  }

  @Override
  public Map<String, Object> getProperties() {
    return m_properties;
  }

  @Override
  public IChartConfig addProperties(IChartConfig config, boolean override) {
    if (config != null && !CollectionUtility.isEmpty(config.getProperties())) {
      Map<String, Object> source = override ? config.getProperties() : m_properties;
      Map<String, Object> target = override ? m_properties : config.getProperties();
      Map<String, Object> propertiesNew = putAllRec(source, target);
      m_properties.clear();
      m_properties.putAll(propertiesNew);
    }
    return this;
  }

  protected Map<String, Object> putAllRec(Map<String, Object> source, Map<String, Object> target) {
    if (CollectionUtility.isEmpty(source) && CollectionUtility.isEmpty(target)) {
      return new HashMap<>();
    }
    else if (CollectionUtility.isEmpty(source)) {
      return new HashMap<>(target);
    }
    else if (CollectionUtility.isEmpty(target)) {
      return new HashMap<>(source);
    }

    Map<String, Object> result = new HashMap<>();

    Set<String> properties = new HashSet<>();
    properties.addAll(source.keySet());
    properties.addAll(target.keySet());

    properties.forEach(property -> {
      Object sourceObj = source.get(property);
      Object targetObj = target.get(property);
      if (sourceObj == null) {
        result.put(property, targetObj);
      }
      else if (sourceObj instanceof Map && targetObj instanceof Map) {
        @SuppressWarnings("unchecked")
        Map<String, Object> sourceMap = (Map<String, Object>) sourceObj;
        @SuppressWarnings("unchecked")
        Map<String, Object> targetMap = (Map<String, Object>) targetObj;
        result.put(property, putAllRec(sourceMap, targetMap));
      }
      else if (sourceObj instanceof List && targetObj instanceof List) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> sourceList = (List<Map<String, Object>>) sourceObj;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> targetList = (List<Map<String, Object>>) targetObj;

        for (int i = 0; i < Math.max(sourceList.size(), targetList.size()); i++) {
          resultList.add(putAllRec(i < sourceList.size() ? sourceList.get(i) : null, i < targetList.size() ? targetList.get(i) : null));
        }

        result.put(property, resultList);
      }
      else {
        result.put(property, sourceObj);
      }
    });

    return result;
  }

  @Override
  public IChartConfig withType(String type) {
    return withProperty(TYPE, type);
  }

  @Override
  public IChartConfig removeType() {
    return removeProperty(TYPE);
  }

  @Override
  public String getType() {
    return (String) getProperty(TYPE);
  }

  @Override
  public IChartConfig withAutoColor(boolean autoColor) {
    return withProperty(AUTO_COLOR, autoColor);
  }

  @Override
  public IChartConfig removeAutoColor() {
    return removeProperty(AUTO_COLOR);
  }

  @Override
  public boolean isAutoColor() {
    return (boolean) getProperty(AUTO_COLOR);
  }

  @Override
  public IChartConfig withMaxSegments(int maxSegments) {
    return withProperty(MAX_SEGMENTS, maxSegments);
  }

  @Override
  public IChartConfig removeMaxSegments() {
    return removeProperty(MAX_SEGMENTS);
  }

  @Override
  public int getMaxSegments() {
    return (int) getProperty(MAX_SEGMENTS);
  }

  @Override
  public IChartConfig withClickable(boolean clickable) {
    return withProperty(CLICKABLE, clickable);
  }

  @Override
  public IChartConfig removeClickable() {
    return removeProperty(CLICKABLE);
  }

  @Override
  public boolean isClickable() {
    return (boolean) getProperty(CLICKABLE);
  }

  @Override
  public IChartConfig withAnimationDuration(int duration) {
    return withProperty(ANIMATION_DURATION, duration);
  }

  @Override
  public IChartConfig removeAnimationDuration() {
    return removeProperty(ANIMATION_DURATION);
  }

  @Override
  public int getAnimationDuration() {
    return (int) getProperty(ANIMATION_DURATION);
  }

  protected int getDefaultAnimationDuration() {
    return 600;
  }

  @Override
  public IChartConfig withAnimated(boolean animated) {
    return withAnimationDuration(animated ? getDefaultAnimationDuration() : 0);
  }

  @Override
  public IChartConfig removeAnimated() {
    return removeAnimationDuration();
  }

  @Override
  public boolean isAnimated() {
    return getAnimationDuration() > 0;
  }

  @Override
  public IChartConfig withTooltipsEnabled(boolean tooltipsEnabled) {
    return withProperty(TOOLTIPS_ENABLED, tooltipsEnabled);
  }

  @Override
  public IChartConfig removeTooltipsEnabled() {
    return removeProperty(TOOLTIPS_ENABLED);
  }

  @Override
  public boolean isTooltipsEnabled() {
    return (boolean) getProperty(TOOLTIPS_ENABLED);
  }

  @Override
  public IChartConfig withLegendDisplay(boolean legendDisplay) {
    return withProperty(LEGEND_DISPLAY, legendDisplay);
  }

  @Override
  public IChartConfig removeLegendDisplay() {
    return removeProperty(LEGEND_DISPLAY);
  }

  @Override
  public boolean isLegendDisplay() {
    return (boolean) getProperty(LEGEND_DISPLAY);
  }

  @Override
  public IChartConfig withLegendClickable(boolean legendClickable) {
    return withProperty(LEGEND_CLICKABLE, legendClickable);
  }

  @Override
  public IChartConfig removeLegendClickable() {
    return removeProperty(LEGEND_CLICKABLE);
  }

  @Override
  public boolean isLegendClickable() {
    return (boolean) getProperty(LEGEND_CLICKABLE);
  }

  @Override
  public IChartConfig withLegendPosition(String legendPosition) {
    return withProperty(LEGEND_POSITION, legendPosition);
  }

  @Override
  public IChartConfig removeLegendPosition() {
    return removeProperty(LEGEND_POSITION);
  }

  @Override
  public String getLegendPosition() {
    return (String) getProperty(LEGEND_POSITION);
  }

  @Override
  public IChartConfig withLegendPositionTop() {
    return withLegendPosition(TOP);
  }

  @Override
  public IChartConfig withLegendPositionBottom() {
    return withLegendPosition(BOTTOM);
  }

  @Override
  public IChartConfig withLegendPositionLeft() {
    return withLegendPosition(LEFT);
  }

  @Override
  public IChartConfig withLegendPositionRight() {
    return withLegendPosition(RIGHT);
  }

  protected IChartConfig withXAxisLabelDisplay(boolean display, int index) {
    return withArrayProperty(X_AXES, index, SCALE_LABEL_DISPLAY, display);
  }

  protected IChartConfig removeXAxisLabelDisplay(int index) {
    return removeArrayProperty(X_AXES, index, SCALE_LABEL_DISPLAY);
  }

  protected boolean isXAxisLabelDisplay(int index) {
    return (boolean) getArrayProperty(X_AXES, index, SCALE_LABEL_DISPLAY);
  }

  @Override
  public IChartConfig withXAxisLabelDisplay(boolean display) {
    return withXAxisLabelDisplay(display, 0);
  }

  @Override
  public IChartConfig removeXAxisLabelDisplay() {
    return removeXAxisLabelDisplay(0);
  }

  @Override
  public boolean isXAxisLabelDisplay() {
    return isXAxisLabelDisplay(0);
  }

  protected IChartConfig withXAxisLabel(String label, int index) {
    return withArrayProperty(X_AXES, index, SCALE_LABEL_LABEL, label);
  }

  protected IChartConfig removeXAxisLabel(int index) {
    return removeArrayProperty(X_AXES, index, SCALE_LABEL_LABEL);
  }

  protected String getXAxisLabel(int index) {
    return (String) getArrayProperty(X_AXES, index, SCALE_LABEL_LABEL);
  }

  @Override
  public IChartConfig withXAxisLabel(String label) {
    return withXAxisLabel(label, 0);
  }

  @Override
  public IChartConfig removeXAxisLabel() {
    return removeXAxisLabel(0);
  }

  @Override
  public String getXAxisLabel() {
    return getXAxisLabel(0);
  }

  protected IChartConfig withYAxisLabelDisplay(boolean display, int index) {
    return withArrayProperty(Y_AXES, index, SCALE_LABEL_DISPLAY, display);
  }

  protected IChartConfig removeYAxisLabelDisplay(int index) {
    return removeArrayProperty(Y_AXES, index, SCALE_LABEL_DISPLAY);
  }

  protected boolean isYAxisLabelDisplay(int index) {
    return (boolean) getArrayProperty(Y_AXES, index, SCALE_LABEL_DISPLAY);
  }

  @Override
  public IChartConfig withYAxisLabelDisplay(boolean display) {
    return withYAxisLabelDisplay(display, 0);
  }

  @Override
  public IChartConfig removeYAxisLabelDisplay() {
    return removeYAxisLabelDisplay(0);
  }

  @Override
  public boolean isYAxisLabelDisplay() {
    return isYAxisLabelDisplay(0);
  }

  protected IChartConfig withYAxisLabel(String label, int index) {
    return withArrayProperty(Y_AXES, index, SCALE_LABEL_LABEL, label);
  }

  protected IChartConfig removeYAxisLabel(int index) {
    return removeArrayProperty(Y_AXES, index, SCALE_LABEL_LABEL);
  }

  protected String getYAxisLabel(int index) {
    return (String) getArrayProperty(Y_AXES, index, SCALE_LABEL_LABEL);
  }

  @Override
  public IChartConfig withYAxisLabel(String label) {
    return withYAxisLabel(label, 0);
  }

  @Override
  public IChartConfig removeYAxisLabel() {
    return removeYAxisLabel(0);
  }

  @Override
  public String getYAxisLabel() {
    return getYAxisLabel(0);
  }
}