package org.pitest.mutationtest.filter;

import org.pitest.functional.Option;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;

public class LimitNumberOfMutationsPerClassFilterFactory implements MutationInterceptorFactory {
  
  private final FeatureParameter limit = FeatureParameter.named("limit")
      .withDescription("Integer value for maximum mutations to create per class");

  @Override
  public String description() {
    return "Max mutations per class limit";
  }

  @Override
  public Feature provides() {    
    return Feature.named("CLASSLIMIT")
        .withDescription("Limits the maximum number of mutations per class")
        .withParameter(limit);
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    Option<Integer> max = params.getInteger(limit);
    if (max.hasNone()) {
      throw new IllegalArgumentException("Max mutation per class filter requires a limit parameter");
    }
    return new LimitNumberOfMutationPerClassFilter(max.value());
  }

}
