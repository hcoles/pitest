package org.pitest.mutationtest.build.intercept.annotations;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class ExcludedAnnotationInterceptorFactory implements MutationInterceptorFactory  {

  @Override
  public String description() {
    return "Excluded annotations plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    return new ExcludedAnnotationInterceptor();
  }

}
