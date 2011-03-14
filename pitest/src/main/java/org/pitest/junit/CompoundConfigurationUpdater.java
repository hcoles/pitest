package org.pitest.junit;

import java.util.Collection;

import org.pitest.extension.Configuration;
import org.pitest.extension.ConfigurationUpdater;

public class CompoundConfigurationUpdater implements ConfigurationUpdater {

  private final Collection<ConfigurationUpdater> children;

  public CompoundConfigurationUpdater(final Collection<ConfigurationUpdater> cus) {
    this.children = cus;
  }

  public Configuration updateConfiguration(final Class<?> clazz,
      final Configuration current) {
    Configuration newConfig = current;
    for (final ConfigurationUpdater each : this.children) {
      newConfig = each.updateConfiguration(clazz, newConfig);
    }
    return newConfig;
  }

}
