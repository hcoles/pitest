package org.pitest.extension.common;

import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.StaticConfiguration;

public class NullStaticConfigUpdater implements StaticConfigUpdater {

  public StaticConfiguration apply(final StaticConfiguration config,
      final Class<?> clazz) {
    return config;
  }

}
