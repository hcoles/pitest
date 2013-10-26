package org.pitest.mutationtest.filter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.classpath.CodeSource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;

public class CompoundFilterFactory implements MutationFilterFactory {
  
  private final List<MutationFilterFactory> children = new ArrayList<MutationFilterFactory>();
  
  public CompoundFilterFactory( Collection<? extends MutationFilterFactory> filters ) {
    this.children.addAll(filters);
  }

  public String description() {
    return null;
  }

  public MutationFilter createFilter(CodeSource source, int maxMutationsPerClass) {
    List<MutationFilter> filters = FCollection.map(children, toFilter(source, maxMutationsPerClass));
    return new CompoundMutationFilter(filters);
  }

  private static F<MutationFilterFactory, MutationFilter> toFilter(final CodeSource source,
      final int maxMutationsPerClass) {
    return new  F<MutationFilterFactory, MutationFilter> () {
      public MutationFilter apply(MutationFilterFactory a) {
       return a.createFilter(source, maxMutationsPerClass);
      }
      
    };
  }

}
