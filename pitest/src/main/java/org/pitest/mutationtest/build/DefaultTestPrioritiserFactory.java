package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;

public class DefaultTestPrioritiserFactory implements TestPrioritiserFactory {

  public String description() {
    return "Default test prioritiser";
  }

  public TestPrioritiser makeTestPrioritiser(CodeSource code,
      CoverageDatabase coverage) {
    return new DefaultTestPrioritiser(coverage);
  }

}
