package org.pitest.mutationtest.build.intercept.staticinitializers;

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
    return new StaticInitializerInterceptor();
  }

}
