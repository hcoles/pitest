package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;

/**
 * Groups mutations within units. Additional precautions must be taken to ensure
 * JVMS do not become poisoned if mutations from different classes are grouped within
 * the same unit. Do not implement unless you understand this and know what you are doing.
 */
public interface MutationGrouperFactory extends ToolClasspathPlugin, ProvidesFeature {

  MutationGrouper makeFactory(Properties props, CodeSource codeSource,
      int numberOfThreads, int unitSize);
}
