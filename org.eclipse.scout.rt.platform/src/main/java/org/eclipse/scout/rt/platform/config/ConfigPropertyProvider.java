/*
 * Copyright (c) 2019 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 */
package org.eclipse.scout.rt.platform.config;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.ServiceLoader;

import org.eclipse.scout.rt.platform.util.StringUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigPropertyProvider implements IPropertyProvider {
  public static final String CLASSPATH_PROTOCOL_NAME = "classpath";
  public static final char PROTOCOL_DELIMITER = ':';
  public static final String CLASSPATH_PREFIX = CLASSPATH_PROTOCOL_NAME + PROTOCOL_DELIMITER;

  private static final Logger LOG = LoggerFactory.getLogger(ConfigPropertyProvider.class);

  private String m_fileUrl;

  /**
   * Create a new instance with the given property url.<br>
   * The identifier can be one of the following:
   * <ul>
   * <li>A {@link String} that contains a valid {@link URL} in external form containing at least one colon (:) to define
   * a schema/protocol.</li>
   * <li>A {@link String} without any protocol or schema that holds an absolute path on the java classpath. Such a
   * string can also have a <code>classpath:</code> prefix.</li>
   * <li>The name of a system property ({@link System#getProperty(String)}) holding a value that corresponds to one of
   * the two previous options.</li>
   * </ul>
   */
  public ConfigPropertyProvider(String fileUrl) {
    m_fileUrl = fileUrl;
  }

  @Override
  public Object getPropertiesIdentifier() {
    return getFileUrl();
  }

  public String getFileUrl() {
    return m_fileUrl;
  }

  @Override
  public List<Entry<String, String>> readProperties() {
    if (getFileUrl() == null) {
      return null;
    }
    URL url = getPropertiesFileUrl(getFileUrl());
    if (url == null) {
      return null;
    }
    return parse(url);
  }

  protected List<Entry<String, String>> parse(URL propertiesFileUrl) {
    String currentDir = extractCurrentDir(propertiesFileUrl);
    List<Entry<String, String>> result = new ArrayList<>();
    IConfigFileLoader loader = getConfigFileLoader();
    Properties props = new Properties() {
      private static final long serialVersionUID = 1L;

      @Override
      public synchronized Object put(Object rawKey, Object rawValue) {
        String key = (String) rawKey;
        String value = (String) rawValue;
        if (value != null && currentDir != null) {
          value = value.replace(PropertiesHelper.CURRENT_DIR_VARIABLE, currentDir);
        }
        Object oldValue = super.put(key, value);
        if (oldValue != null) {
          logDuplicateKey(rawKey, oldValue, rawValue);
        }
        if (StringUtility.hasText(key)) {
          result.add(new AbstractMap.SimpleEntry<>(key, value));
        }
        return oldValue;
      }
    };

    LOG.info("Reading properties from {} using {}", propertiesFileUrl, loader.getClass().getName());
    loader.load(propertiesFileUrl, props);
    return result;
  }

  /**
   * define value for ${CURRENT_DIR}
   */
  protected String extractCurrentDir(URL propertiesFileUrl) {
    if ("file".equals(propertiesFileUrl.getProtocol())) {
      try {
        return new File(propertiesFileUrl.toURI().getSchemeSpecificPart()).getParentFile().getAbsolutePath();
      }
      catch (URISyntaxException e) {
        LOG.warn("Cannot extract path from '{}'", propertiesFileUrl, e);
      }
    }
    return null;
  }

  protected IConfigFileLoader getConfigFileLoader() {
    IConfigFileLoader loader = null;
    ServiceLoader<IConfigFileLoader> services = ServiceLoader.load(IConfigFileLoader.class);
    for (IConfigFileLoader service : services) {
      loader = service;
      if (loader != null) {
        break;
      }
    }
    if (loader == null) {
      loader = new DefaultConfigFileLoader();
    }
    return loader;
  }

  protected void logDuplicateKey(Object key, Object oldValue, Object newValue) {
    LOG.warn("Duplicate config key: '{}'. Old value '{}' replaced with '{}'.", key, oldValue, newValue);
  }

  protected static URL getPropertiesFileUrl(String filePath) {
    if (!StringUtility.hasText(filePath)) {
      return null;
    }
    String sysPropFileName = System.getProperty(filePath);
    if (StringUtility.hasText(sysPropFileName)) {
      filePath = sysPropFileName;
    }
    return getResourceUrl(filePath);
  }

  /**
   * Parses the file path specified to an {@link URL}.
   * <p>
   * The method supports the classpath prefix (see {@link #CLASSPATH_PREFIX}) for resources that should be searched on
   * the classpath. Besides classpath resources also all installed URL schemes and absolute local file paths are
   * supported.
   * <p>
   * <b>Examples for classpath:</b>
   * <ul>
   * <li>classpath:external-config.properties</li>
   * <li>classpath:myfolder/config.properties</li>
   * </ul>
   * <p>
   * <b>Examples for absolute path:</b>
   * <ul>
   * <li>file:/var/etc/...</li>
   * <li>file:/C:/path/to/my/file.ext</li>
   * <li>/var/etc/...</li>
   * <li>C:\foo\bar\...</li>
   * </ul>
   * @param filePath
   *          The absolute file path. May be {@code null}.
   * @return An {@link URL} pointing to the file if it can be found. {@code null} otherwise.
   */
  public static URL getResourceUrl(String filePath) {
    if (!StringUtility.hasText(filePath)) {
      return null;
    }

    if (filePath.startsWith(CLASSPATH_PREFIX)) {
      filePath = filePath.substring(CLASSPATH_PREFIX.length());
      if (!StringUtility.hasText(filePath)) {
        return null;
      }
      return PropertiesHelper.class.getClassLoader().getResource(filePath);
    }
    else{
      return toPropertiesFileUrl(filePath);
    }
  }

  protected static URL toPropertiesFileUrl(String filePath) {
    try {
      return new URL(filePath);
    }
    catch (MalformedURLException e) {
      LOG.debug("Config file path '{}' is no valid URL. Trying to parse as absolute file path.", filePath, e);
    }

    try {
      File local = new File(filePath);
      if (local.isFile() && local.isAbsolute()) {
        return local.toURI().toURL();
      }
      else {
        throw new IllegalArgumentException("Invalid config file path: '" + filePath + "'.");
      }
    }
    catch (Exception e) {
      throw new IllegalArgumentException("Unable to load config file with URL '" + filePath + "'.", e);
    }
  }
}