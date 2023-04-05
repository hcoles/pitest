package org.pitest.mutationtest.config;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.help.PitHelpError;
import org.pitest.junit.NullConfiguration;
import org.pitest.mutationtest.environment.CompositeReset;
import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.testapi.Configuration;
import org.pitest.util.PitError;

import java.util.List;
import java.util.stream.Collectors;

import static org.pitest.help.Help.NO_TEST_PLUGIN;

public class MinionSettings {

  private final ClientPluginServices plugins;

  public MinionSettings(final ClientPluginServices plugins) {
    this.plugins = plugins;
  }

  public ResetEnvironment createReset() {
    List<ResetEnvironment> resets = this.plugins.findResets().stream()
            .map(EnvironmentResetPlugin::make).collect(Collectors.toList());
    return new CompositeReset(resets);
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
            // hack until interface updated to return optional
            .filter(c -> !(c instanceof NullConfiguration))
            .collect(Collectors.toList());

    if (configurations.isEmpty()) {
      throw new PitHelpError(NO_TEST_PLUGIN);
    }

    return new PrioritisingTestConfiguration(configurations);

  }


}
