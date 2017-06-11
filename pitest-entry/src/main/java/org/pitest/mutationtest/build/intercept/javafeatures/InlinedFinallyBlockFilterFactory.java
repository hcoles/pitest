package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.CompoundMutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class InlinedFinallyBlockFilterFactory  implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Inlined finally block filter plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    if (data.isDetectInlinedCode()) {
      return new InlinedFinallyBlockFilter();
    }
    return CompoundMutationInterceptor.nullInterceptor();
  }

}
