package org.pitest.mutationtest.build.intercept.timeout;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class InfiniteIteratorLoopFilterFactory  implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Long running iterator loop filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new InfiniteIteratorLoopFilter();
  }

  @Override
  public Feature provides() {
    return Feature.named("FINFIT")
        .withOnByDefault(true)
        .withDescription("Filters mutations that may cause infinite loops"
            + " by removing calls to iterator.next");
  }

}
