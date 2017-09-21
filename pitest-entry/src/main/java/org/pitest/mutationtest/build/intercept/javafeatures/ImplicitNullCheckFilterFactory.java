package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class ImplicitNullCheckFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Implicit null check filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new ImplicitNullCheckFilter();
  }
  
  @Override
  public Feature provides() {
    return Feature.named("FINULL")
        .withOnByDefault(true)
        .withDescription("Filters mutations in compiler generated code that checks for null by calling getClass");
  }

}
