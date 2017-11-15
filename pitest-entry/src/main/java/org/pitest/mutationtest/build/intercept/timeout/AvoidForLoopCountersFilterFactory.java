package org.pitest.mutationtest.build.intercept.timeout;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class AvoidForLoopCountersFilterFactory implements MutationInterceptorFactory  {
  @Override
  public String description() {
    return "For loop counter filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new AvoidForLoopCounterFilter();
  }

  @Override
  public Feature provides() {
    return Feature.named("FFLOOP")
        .withOnByDefault(true)
        .withDescription("Filters any mutations to increments in for loops as they may cause timeouts");
  }

}
