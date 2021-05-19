package org.pitest.mutationtest.config;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.testapi.Configuration;
import org.pitest.util.PitError;

import java.util.List;
import java.util.stream.Collectors;

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
    List<Configuration> configurations = this.plugins.findTestFrameworkPlugins().stream()
            .map(p -> p.createTestFrameworkConfiguration(options.getGroupConfig(),
                    source,
                    options.getExcludedRunners(),
                    options.getIncludedTestMethods()))
            .collect(Collectors.toList());

    return new PrioritisingTestConfiguration(configurations);

  }

}
