package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.ToolClasspathPlugin;

public interface MutationGrouperFactory extends ToolClasspathPlugin {

  public MutationGrouper makeFactory(CodeSource codeSource, int numberOfThreads, int unitSize);
}
