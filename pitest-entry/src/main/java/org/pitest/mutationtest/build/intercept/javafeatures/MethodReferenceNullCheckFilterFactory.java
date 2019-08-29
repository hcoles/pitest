package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class MethodReferenceNullCheckFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Method reference null check filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new MethodReferenceNullCheckFilter();
  }

  @Override
  public Feature provides() {
    return Feature.named("FMRNULL")
        .withOnByDefault(true)
        .withDescription("Filters mutations in compiler generated code that inserts Objects.requireNonNull for method references");
  }

}