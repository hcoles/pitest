package org.pitest.mutationtest.config;

import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;

/**
 * Plugin that provides support for both JUnit 3, 4 and TestNG.
 *
 * This plugin has been battle tested against many different strange uses and
 * abuses of JUnit found in real world code bases.
 *
 * It will probably work well on your legacy code base but some of the code is
 * pretty hairy, so support for more modern test frameworks will likely be added
 * via different plugins.
 *
 * Much of the complexity comes from splitting the tests down into smaller
 * units.
 *
 * Note. No service locator property file is provided for this plugin, it is
 * hard coded to be used when no other is provided on the classpath.
 */
public class LegacyTestFrameworkPlugin implements TestPluginFactory {

  @Override
  public String description() {
    return "Default test framework support";
  }

  @Override
  public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
      ClassByteArraySource source, Collection<String> excludedRunners) {
    final ConfigurationFactory configFactory = new ConfigurationFactory(config,
        source, excludedRunners);
    return configFactory.createConfiguration();
  }

}
