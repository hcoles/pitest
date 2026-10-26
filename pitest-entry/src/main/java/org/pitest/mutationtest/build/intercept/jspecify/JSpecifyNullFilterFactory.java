package org.pitest.mutationtest.build.intercept.jspecify;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class JSpecifyNullFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "JSpecify nullness filter";
  }

  @Override
  public Feature provides() {
    return Feature.named("JSPECIFY")
        .withOnByDefault(true)
        .withDescription("Filters null return mutations in code declared non-null by JSpecify @NullMarked");
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new JSpecifyNullFilter(params.source());
  }

}
