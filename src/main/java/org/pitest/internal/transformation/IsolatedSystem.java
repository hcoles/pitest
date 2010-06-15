/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.internal.transformation;

import java.util.Properties;

/**
 * @author henry
 * 
 */
public class IsolatedSystem {

  private static Properties props;

  static {
    try {
      performPropertiesCheck();
      props = (Properties) System.getProperties().clone();
    } catch (final SecurityException e) {
      // swallow and ignore
    }
  }

  public static String getProperty(final String key) {
    performPropertiesCheck();
    return props.getProperty(key);
  }

  public static String getProperty(final String key, final String defaultValue) {
    final String value = getProperty(key);
    if (value == null) {
      return defaultValue;
    } else {
      return value;
    }
  }

  public static String setProperty(final String key, final String value) {
    performPropertiesCheck();
    return (String) props.setProperty(key, value);
  }

  public static Properties getProperties() {
    performPropertiesCheck();
    return props;
  }

  public static void setProperties(final Properties props) {
    performPropertiesCheck();
    IsolatedSystem.props = props;
  }

  private static void performPropertiesCheck() {
    final SecurityManager sm = getSecurityManager();
    if (sm != null) {
      sm.checkPropertiesAccess();
    }

  }

  private static SecurityManager getSecurityManager() {
    return System.getSecurityManager();
  }

}
