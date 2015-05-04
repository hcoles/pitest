package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;

public class DefaultMutationGrouperFactory implements MutationGrouperFactory {

  public String description() {
    return "Default mutation grouping";
  }

  public MutationGrouper makeFactory(final Properties props,
      final CodeSource codeSource, final int numberOfThreads, final int unitSize) {
    return new DefaultGrouper(unitSize);
  }

}
