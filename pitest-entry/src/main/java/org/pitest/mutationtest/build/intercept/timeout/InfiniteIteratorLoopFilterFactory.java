package org.pitest.mutationtest.build.intercept.timeout;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class InfiniteIteratorLoopFilterFactory  implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Long running iterator loop filter";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    return new InfiniteIteratorLoopFilter();
  }

}
