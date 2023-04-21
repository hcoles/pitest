package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureParameter;
import org.pitest.plugin.FeatureSetting;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public final class InterceptorParameters {

  private final FeatureSetting conf;
  private final ReportOptions data;
  private final ClassByteArraySource source;
  private final CoverageDatabase coverage;

  private final TestPrioritiser testPrioritiser;


  public InterceptorParameters(FeatureSetting conf, ReportOptions data, CoverageDatabase coverage,
      ClassByteArraySource source, TestPrioritiser testPrioritiser) {
    this.conf = conf;
    this.data = data;
    this.coverage = coverage;
    this.source = source;
    this.testPrioritiser = testPrioritiser;
  }

  public ReportOptions data() {
    return this.data;
  }

  public CoverageDatabase coverage() {
    return this.coverage;
  }

  public Optional<FeatureSetting> settings() {
    return Optional.ofNullable(this.conf);
  }


  public ClassByteArraySource source() {
    return this.source;
  }

  public TestPrioritiser testPrioritiser() {
    return this.testPrioritiser;
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
    return val.map(Integer::parseInt);
  }

}
