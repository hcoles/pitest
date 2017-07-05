package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class TryWithResourcesFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Try with resources filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new TryWithResourcesFilter();
  }
  
  @Override
  public Feature provides() {
    return Feature.named("FTRYWR")
        .withOnByDefault(true)
        .withDescription("Filters mutations in code generated for try with resources statements");
  }

}
