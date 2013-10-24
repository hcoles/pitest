package org.pitest.mutationtest.build;

import org.pitest.classpath.CodeSource;

public class DefaultMutationGrouperFactory implements MutationGrouperFactory {

  public String description() {
    return "Default mutation grouping";
  }

  public MutationGrouper makeFactory(CodeSource codeSource,
      int numberOfThreads, int unitSize) {
    return new DefaultGrouper(unitSize);
  }

}
