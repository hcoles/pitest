package org.pitest.mutationtest.config;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

import org.pitest.testapi.TestGroupConfig;

public class TestPluginArguments implements Serializable {

  private static final long serialVersionUID = 1L;

  private final TestGroupConfig groupConfig;
  private final Collection<String> includedTestMethods;
  private final Collection<String> excludedRunners;
  private final boolean skipFailingTests;

  public TestPluginArguments(TestGroupConfig groupConfig,
      Collection<String> excludedRunners,
      Collection<String> includedTestMethods,
      boolean skipFailingTests) {
    Objects.requireNonNull(groupConfig);
    Objects.requireNonNull(excludedRunners);
    this.groupConfig = groupConfig;
    this.excludedRunners = excludedRunners;
    this.includedTestMethods = includedTestMethods;
    this.skipFailingTests = skipFailingTests;
  }

  public static TestPluginArguments defaults() {
    return new TestPluginArguments(new TestGroupConfig(), Collections.emptyList(),
            Collections.emptyList(), false);
  }

  public TestGroupConfig getGroupConfig() {
    return this.groupConfig;
  }

  public Collection<String> getExcludedRunners() {
    return this.excludedRunners;
  }

  public Collection<String> getIncludedTestMethods() {
    return this.includedTestMethods;
  }

  public boolean skipFailingTests() {
    return this.skipFailingTests;
  }

}
