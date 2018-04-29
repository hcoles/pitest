package org.pitest.mutationtest.config;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.PitError;

public class MinionSettings {

  private final ClientPluginServices plugins;

  public MinionSettings(final ClientPluginServices plugins) {
    this.plugins = plugins;
  }

  public MutationEngineFactory createEngine(String engine) {
    for (final MutationEngineFactory each : this.plugins.findMutationEngines()) {
      if (each.name().equals(engine)) {
        return each;
      }
    }
    throw new PitError("Could not load requested engine "
        + engine);
  }


  public Configuration getTestFrameworkPlugin(TestPluginArguments options, ClassByteArraySource source) {
    for (final TestPluginFactory each : this.plugins.findTestFrameworkPlugins()) {
      if (each.name().equals(options.getTestPlugin())) {
        return each.createTestFrameworkConfiguration(options.getGroupConfig(),
            source,
            options.getExcludedRunners(),
            options.getIncludedTestMethods());
      }
    }
    throw new PitError("Could not load requested test plugin "
        + options.getTestPlugin());
  }

}
