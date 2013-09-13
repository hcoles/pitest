package org.pitest.mutationtest;

import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.mutationtest.report.SourceLocator;

public class ListenerArguments {
  
  private final ResultOutputStrategy outputStrategy;
  private final CoverageDatabase coverage;
  private final long startTime;
  private final SourceLocator locator;
  
  public ListenerArguments(ResultOutputStrategy outputStrategy, CoverageDatabase coverage, SourceLocator locator, long startTime) {
    this.outputStrategy = outputStrategy;
    this.coverage = coverage;
    this.locator = locator;
    this.startTime = startTime;
  }

  public ResultOutputStrategy getOutputStrategy() {
    return outputStrategy;
  }

  public CoverageDatabase getCoverage() {
    return coverage;
  }

  public long getStartTime() {
    return startTime;
  }

  public SourceLocator getLocator() {
    return locator;
  }

  
  
}
