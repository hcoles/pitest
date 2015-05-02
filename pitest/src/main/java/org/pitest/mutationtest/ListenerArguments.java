package org.pitest.mutationtest;

import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.util.ResultOutputStrategy;

import java.util.Properties;

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
  private final Properties           pluginProperties;

  public ListenerArguments(final ResultOutputStrategy outputStrategy,
      final CoverageDatabase coverage, final SourceLocator locator,
      final MutationEngine engine, final long startTime, final Properties pluginProperties) {
    this.outputStrategy = outputStrategy;
    this.coverage = coverage;
    this.locator = locator;
    this.startTime = startTime;
    this.engine = engine;
    this.pluginProperties = pluginProperties;
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

  public Properties getPluginProperties() { return this.pluginProperties; }
}
