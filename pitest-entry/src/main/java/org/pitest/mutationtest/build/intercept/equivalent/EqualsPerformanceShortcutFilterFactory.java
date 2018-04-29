package org.pitest.mutationtest.build.intercept.equivalent;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class EqualsPerformanceShortcutFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Equals shortcut equivalent mutant filter";
  }

  @Override
  public Feature provides() {
    return Feature.named("FSEQUIVEQUALS")
        .withOnByDefault(true)
        .withDescription("Filters equivalent mutations that affect only performance in short cutting equals methods");
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new EqualsPerformanceShortcutFilter();
  }

}
