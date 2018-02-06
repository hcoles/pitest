package org.pitest.mutationtest.build;

import java.util.Collections;
import java.util.List;

import org.pitest.classinfo.ClassByteArraySource;
import java.util.Optional;
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
    return this.data;
  }

  public Optional<FeatureSetting> settings() {
    return Optional.ofNullable(this.conf);
  }


  public ClassByteArraySource source() {
    return this.source;
  }

  public Optional<String> getString(FeatureParameter limit) {
    if (this.conf == null) {
      return Optional.empty();
    }
    return this.conf.getString(limit.name());
  }

  public List<String> getList(FeatureParameter key) {
    if (this.conf == null) {
      return Collections.emptyList();
    }
    return this.conf.getList(key.name());
  }

  public Optional<Integer> getInteger(FeatureParameter key) {
    final Optional<String> val = getString(key);
    if (val.isPresent()) {
      return Optional.ofNullable(Integer.parseInt(val.get()));
    }
    return Optional.empty();
  }

}
