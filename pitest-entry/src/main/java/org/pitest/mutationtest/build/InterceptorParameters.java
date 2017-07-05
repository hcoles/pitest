package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.Option;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSetting;

public final class InterceptorParameters {
  
  private final FeatureSetting conf;
  private final ReportOptions data;
  private final ClassByteArraySource source;
  

  public InterceptorParameters(FeatureSetting conf, ReportOptions data,
      ClassByteArraySource source) {
    this.conf = conf;
    this.data = data;
    this.source = source;
  }

  public ReportOptions data() {
    return data;
  }
  
  public Option<FeatureSetting> settings() {
    return Option.some(conf);
  }


  public ClassByteArraySource source() {
    return source;
  }
  
}
