package org.pitest.mutationtest.build.intercept;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.CompoundMutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class StaticInitializerFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Static initializer filter plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    if (data.isMutateStaticInitializers()) {
      return CompoundMutationInterceptor.nullInterceptor();
    }
    return new StaticInitializerFilter();
  }

}
