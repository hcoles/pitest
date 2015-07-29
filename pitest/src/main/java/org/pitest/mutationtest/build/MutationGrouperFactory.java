package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.ToolClasspathPlugin;

public interface MutationGrouperFactory extends ToolClasspathPlugin {

  MutationGrouper makeFactory(Properties props, CodeSource codeSource,
      int numberOfThreads, int unitSize);
}
