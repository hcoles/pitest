package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.plugin.ToolClasspathPlugin;

public interface TestPrioritiserFactory extends ToolClasspathPlugin {

  public TestPrioritiser makeTestPrioritiser(CodeSource code, CoverageDatabase coverage);
  
}
