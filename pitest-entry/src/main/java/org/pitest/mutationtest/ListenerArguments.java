package org.pitest.mutationtest;

import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.util.ResultOutputStrategy;

/**
 * Data passed to the listener MutationResultListener factories for use when
 * constructing listeners.
 */
public class ListenerArguments {

  private final ResultOutputStrategy outputStrategy;
  private final CoverageDatabase     coverage;
  private final long                 startTime;
  private final SourceLocator        locator;
  private final MutationEngine       engine;
  private final boolean              fullMutationMatrix;
  private final ReportOptions        data;

  public ListenerArguments(ResultOutputStrategy outputStrategy,
                           CoverageDatabase coverage,
                           SourceLocator locator,
                           MutationEngine engine,
                           long startTime,
                           boolean fullMutationMatrix,
                           ReportOptions        data) {
    this.outputStrategy = outputStrategy;
    this.coverage = coverage;
    this.locator = locator;
    this.startTime = startTime;
    this.engine = engine;
    this.fullMutationMatrix = fullMutationMatrix;
    this.data = data;
  }

  public ResultOutputStrategy getOutputStrategy() {
    return this.outputStrategy;
  }

  public CoverageDatabase getCoverage() {
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
}
