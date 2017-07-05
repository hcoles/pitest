package org.pitest.mutationtest.build.intercept.logging;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class LoggingCallsFilterFactory  implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Logging calls filter";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new LoggingCallsFilter(params.data().getLoggingClasses());
  }

  @Override
  public Feature provides() {
    return Feature.named("FLOGCALL")
        .withOnByDefault(true)
        .withDescription("Filters mutations in code that makes calls to logging frameworks");
  }
  
}
