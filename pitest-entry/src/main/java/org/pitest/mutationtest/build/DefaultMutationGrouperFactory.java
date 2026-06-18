package org.pitest.mutationtest.build;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.plugin.Feature;

public class DefaultMutationGrouperFactory implements MutationGrouperFactory {

  @Override
  public String description() {
    return "Default mutation grouping";
  }

  @Override
  public MutationGrouper makeFactory(final Properties props,
      final CodeSource codeSource, final int numberOfThreads, final int unitSize) {
    return new DefaultGrouper(unitSize);
  }

  @Override
  public Feature provides() {
    return Feature.named("defaultgrouper")
            .asInternalFeature()
            .withOnByDefault(true)
            .withDescription(description());
  }
}
