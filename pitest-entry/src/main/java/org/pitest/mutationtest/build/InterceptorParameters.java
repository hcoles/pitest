package org.pitest.mutationtest.build;

import java.util.Collections;
import java.util.List;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.Option;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureParameter;
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

  public Option<String> getString(FeatureParameter limit) {
    if (conf == null) {
      return Option.none();
    }
    return conf.getString(limit.name());
  }
  
  public List<String> getList(FeatureParameter key) {
    if (conf == null) {
      return Collections.emptyList();
    }
    return conf.getList(key.name());
  }
  
  public Option<Integer> getInteger(FeatureParameter key) {
    Option<String> val = getString(key);
    if (val.hasSome()) {
      return Option.some(Integer.parseInt(val.value()));
    }
    return Option.none();
  }
  
}
