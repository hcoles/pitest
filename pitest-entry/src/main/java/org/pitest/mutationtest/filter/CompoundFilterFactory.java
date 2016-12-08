package org.pitest.mutationtest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class CompoundFilterFactory implements MutationFilterFactory {

  private final List<MutationFilterFactory> children = new ArrayList<MutationFilterFactory>();

  public CompoundFilterFactory(
      Collection<? extends MutationFilterFactory> filters) {
    this.children.addAll(filters);
  }

  @Override
  public String description() {
    return null;
  }

  @Override
  public MutationFilter createFilter(Properties props, CodeSource source,
      int maxMutationsPerClass) {
    List<MutationFilter> filters = FCollection.map(this.children,
        toFilter(props, source, maxMutationsPerClass));
    return new CompoundMutationFilter(filters);
  }

  private static F<MutationFilterFactory, MutationFilter> toFilter(
      final Properties props, final CodeSource source,
      final int maxMutationsPerClass) {
    return new F<MutationFilterFactory, MutationFilter>() {
      @Override
      public MutationFilter apply(MutationFilterFactory a) {
        return a.createFilter(props, source, maxMutationsPerClass);
      }

    };
  }

}
