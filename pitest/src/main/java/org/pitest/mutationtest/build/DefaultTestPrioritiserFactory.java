package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;

import java.util.Properties;

public class DefaultTestPrioritiserFactory implements TestPrioritiserFactory {

  public String description() {
    return "Default test prioritiser";
  }

  public TestPrioritiser makeTestPrioritiser(CodeSource code,
      CoverageDatabase coverage, Properties pluginProperties) {
    return new DefaultTestPrioritiser(coverage);
  }

}
