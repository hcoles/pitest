package org.pitest.extension.common;

import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;

public class NullConfigurationUpdater implements ConfigurationUpdater {

  public Configuration updateConfiguration(final Class<?> clazz,
      final Configuration current) {
    return current;
  }

}
