package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.mutationtest.tdg.Tdgimpl;
public interface TestPrioritiserFactory extends ToolClasspathPlugin {

  TestPrioritiser makeTestPrioritiser(Properties props, CodeSource code,
  Tdgimpl tdg);

  // TestPrioritiser makeTestPrioritiser(Properties props, CodeSource code,
  // CoverageDatabase coverage);

}
