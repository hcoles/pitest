package org.pitest.mutationtest.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;

import org.pitest.junit.JUnitTestPlugin;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Preconditions;

public class TestPluginArguments implements Serializable {

  private static final long serialVersionUID = 1L;
  
  private final String testPlugin;
  private final TestGroupConfig groupConfig;
  private final Collection<String> includedTestMethods;
  private final Collection<String> excludedRunners;
   
  public TestPluginArguments(String testPlugin,
      TestGroupConfig groupConfig,
      Collection<String> excludedRunners,
      Collection<String> includedTestMethods) {
    Preconditions.checkNotNull(testPlugin);
    Preconditions.checkNotNull(groupConfig);
    Preconditions.checkNotNull(excludedRunners);    
    this.testPlugin = testPlugin;
    this.groupConfig = groupConfig;
    this.excludedRunners = excludedRunners;
    this.includedTestMethods = includedTestMethods;
  }

  public static TestPluginArguments defaults() {
    return new TestPluginArguments(JUnitTestPlugin.NAME, new TestGroupConfig(), Collections.<String>emptyList(),
            Collections.<String>emptyList());
  }
  
  public TestPluginArguments withTestPlugin(String plugin) {
    return new TestPluginArguments(plugin, groupConfig, excludedRunners, includedTestMethods);
  }
  
  public TestGroupConfig getGroupConfig() {
    return groupConfig;
  }

  public Collection<String> getExcludedRunners() {
    return excludedRunners;
  }

  public Collection<String> getIncludedTestMethods() {
    return includedTestMethods;
  }

  public String getTestPlugin() {
    return testPlugin;
  }
    
}
