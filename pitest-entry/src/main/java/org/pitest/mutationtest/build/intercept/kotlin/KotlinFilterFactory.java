package org.pitest.mutationtest.build.intercept.kotlin;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class KotlinFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Kotlin junk mutations filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new KotlinFilter();
  }

  @Override
  public Feature provides() {
    return Feature.named("FKOTLIN")
        .withDescription("Filters out junk mutations in bytecode created by compiler for kotlin language features")
        .withOnByDefault(true);
  }

}
