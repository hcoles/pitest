package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class TryWithResourcesFilterFactory implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Try with resources filter";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    return new TryWithResourcesFilter();
  }

}
