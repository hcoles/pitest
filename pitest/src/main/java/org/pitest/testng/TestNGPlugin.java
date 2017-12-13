package org.pitest.testng;

import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestPluginFactory;

public class TestNGPlugin implements TestPluginFactory {

  @Override
  public String description() {
    return "TestNG plugin";
  }

  @Override
  public Configuration createTestFrameworkConfiguration(TestGroupConfig config,
      ClassByteArraySource source, Collection<String> excludedRunners) {
    return new TestNGConfiguration(config);
  }

  @Override
  public String name() {
    return "testng";
  }

}