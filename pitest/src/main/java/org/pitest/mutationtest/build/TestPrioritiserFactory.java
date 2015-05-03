package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.plugin.ToolClasspathPlugin;

import java.util.Properties;

public interface TestPrioritiserFactory extends ToolClasspathPlugin {

  TestPrioritiser makeTestPrioritiser(CodeSource code, CoverageDatabase coverage, Properties pluginProperties);
  
}
