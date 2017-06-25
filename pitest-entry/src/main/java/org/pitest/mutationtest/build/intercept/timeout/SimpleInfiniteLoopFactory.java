package org.pitest.mutationtest.build.intercept.timeout;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class SimpleInfiniteLoopFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Infinite loop interceptor";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    return new SimpleInfiniteLoopInterceptor();
  }

}
