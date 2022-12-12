package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.tdg.Tdgimpl;
public class AllTestPrioritiserFactory implements TestPrioritiserFactory {

  @Override
  public String description() {
    return "Default test prioritiser";
  }

  @Override
  public TestPrioritiser makeTestPrioritiser(final Properties props,
      final CodeSource code, final Tdgimpl tdg) {
    return new AllTestPrioritiser(tdg);
  }

}
