package org.pitest;

import java.util.Collection;

import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.StaticConfiguration;

public class CompoundStaticConfigurationUpdater implements StaticConfigUpdater {

  private final Collection<StaticConfigUpdater> children;

  public CompoundStaticConfigurationUpdater(
      final Collection<StaticConfigUpdater> children) {
    this.children = children;
  }

  public StaticConfiguration apply(final StaticConfiguration config,
      final Class<?> clazz) {
    StaticConfiguration staticConfig = new DefaultStaticConfig(config);
    for (final StaticConfigUpdater each : this.children) {
      staticConfig = each.apply(staticConfig, clazz);
    }

    return staticConfig;

  }

}
