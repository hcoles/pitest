package org.pitest.mutationtest.build.intercept.timeout;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class InfiniteForLoopFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Infinite for loop filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new InfiniteForLoopFilter();
  }

  @Override
  public Feature provides() {
    return Feature.named("FINFINC")
        .withOnByDefault(true)
        .withDescription("Filters out mutations that may cause infinite loops"
            + " by preventing modifications to a loop counter in a for or while loop");
  }

}
