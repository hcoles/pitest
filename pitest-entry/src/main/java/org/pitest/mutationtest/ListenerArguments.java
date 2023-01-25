package org.pitest.mutationtest;

import org.pitest.coverage.ReportCoverage;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.plugin.FeatureSetting;
import org.pitest.util.ResultOutputStrategy;

import java.util.Optional;

/**
 * Data passed to the listener MutationResultListener factories for use when
 * constructing listeners.
 */
public class ListenerArguments {

  private final ResultOutputStrategy outputStrategy;
  private final ReportCoverage       coverage;
  private final long                 startTime;
  private final SourceLocator        locator;
  private final MutationEngine       engine;
  private final boolean              fullMutationMatrix;
  private final ReportOptions        data;
  private final FeatureSetting       setting;

  public ListenerArguments(ResultOutputStrategy outputStrategy,
                           ReportCoverage coverage,
                           SourceLocator locator,
                           MutationEngine engine,
                           long startTime,
                           boolean fullMutationMatrix,
                           ReportOptions  data) {
    this(outputStrategy, coverage, locator, engine, startTime, fullMutationMatrix, data, null);
  }

  ListenerArguments(ResultOutputStrategy outputStrategy,
                           ReportCoverage coverage,
                           SourceLocator locator,
                           MutationEngine engine,
                           long startTime,
                           boolean fullMutationMatrix,
                           ReportOptions  data,
                           FeatureSetting setting) {
    this.outputStrategy = outputStrategy;
    this.coverage = coverage;
    this.locator = locator;
    this.startTime = startTime;
    this.engine = engine;
    this.fullMutationMatrix = fullMutationMatrix;
    this.data = data;
    this.setting = setting;
  }

  public ResultOutputStrategy getOutputStrategy() {
    return this.outputStrategy;
  }

  public ReportCoverage getCoverage() {
    return this.coverage;
  }

  public long getStartTime() {
    return this.startTime;
  }

  public SourceLocator getLocator() {
    return this.locator;
  }

  public MutationEngine getEngine() {
    return this.engine;
  }

  public boolean isFullMutationMatrix() {
  return fullMutationMatrix;
  }

  public ReportOptions data() {
    return data;
  }

  public Optional<FeatureSetting> settings() {
    return Optional.ofNullable(setting);
  }

  public ListenerArguments withSetting(FeatureSetting setting) {
    return new ListenerArguments(outputStrategy,
            coverage,
            locator,
            engine,
            startTime,
            fullMutationMatrix,
            data,
            setting);
  }

}
