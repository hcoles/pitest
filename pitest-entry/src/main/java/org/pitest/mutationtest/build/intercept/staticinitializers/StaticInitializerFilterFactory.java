package org.pitest.mutationtest.build.intercept.staticinitializers;

import org.pitest.mutationtest.build.CompoundMutationInterceptor;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class StaticInitializerFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Static initializer filter plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    if (params.data().isMutateStaticInitializers()) {
      return CompoundMutationInterceptor.nullInterceptor();
    }
    return new StaticInitializerFilter();
  }

  @Override
  public Feature provides() {
    return Feature.named("FSTATINIT")
        .withOnByDefault(true)
        .withDescription("Filters mutations in static initializers and code called only from them");
  }
  
}
