package org.pitest.junit;

import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.Preconditions;

/**
 * Plugin that provides support for both JUnit 3, 4
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
 */
public class JUnitTestPlugin implements TestPluginFactory {

  public static final String NAME = "junit";

  @Override
  public String description() {
    return "JUnit plugin";
  }

  @Override
  public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
      ClassByteArraySource source, Collection<String> excludedRunners, Collection<String> includedTestMethods) {
    Preconditions.checkNotNull(config);
    return new JUnitCompatibleConfiguration(config, excludedRunners, includedTestMethods);
  }

  @Override
  public String name() {
    return NAME;
  }

}
