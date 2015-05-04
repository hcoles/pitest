package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;

public class DefaultTestPrioritiserFactory implements TestPrioritiserFactory {

  public String description() {
    return "Default test prioritiser";
  }

  public TestPrioritiser makeTestPrioritiser(final Properties props,
      final CodeSource code, final CoverageDatabase coverage) {
    return new DefaultTestPrioritiser(coverage);
  }

}
