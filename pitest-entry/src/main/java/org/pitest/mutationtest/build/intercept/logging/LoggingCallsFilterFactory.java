package org.pitest.mutationtest.build.intercept.logging;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.ReportOptions;

public class LoggingCallsFilterFactory  implements MutationInterceptorFactory {

  @Override
  public String description() {
    return "Logging calls filter";
  }

  @Override
  public MutationInterceptor createInterceptor(ReportOptions data,
      ClassByteArraySource source) {
    return new LoggingCallsFilter(data.getLoggingClasses());
  }

}
