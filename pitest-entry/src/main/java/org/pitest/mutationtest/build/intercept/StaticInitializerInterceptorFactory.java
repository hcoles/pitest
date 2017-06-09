package org.pitest.mutationtest.build.intercept;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class StaticInitializerInterceptorFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Static initializer code detector plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    StaticInitializerInterceptor analyser = new StaticInitializerInterceptor();
    if (data.isMutateStaticInitializers()) {
      return analyser;
    }
    return new StaticInitializerFilter(analyser);
  }

}
