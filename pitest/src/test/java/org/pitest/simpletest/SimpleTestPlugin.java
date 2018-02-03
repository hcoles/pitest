package org.pitest.simpletest;

import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;

public class SimpleTestPlugin implements TestPluginFactory {

  public static final String NAME = "fortestingonly";

  @Override
  public String description() {
    return "Simple plugin for testing";
  }

  @Override
  public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
      ClassByteArraySource source, Collection<String> excludedRunners, Collection<String> includedMethods) {
    return new ConfigurationForTesting();
  }

  @Override
  public String name() {
    return NAME;
  }

}
