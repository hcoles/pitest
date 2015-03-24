package org.pitest.mutationtest.tooling;

import org.pitest.functional.Option;

public final class AnalysisResult {

  private final Option<CombinedStatistics> statistics;
  private final Option<Exception>          error;

  private AnalysisResult(final CombinedStatistics statistics,
      final Exception error) {
    this.statistics = Option.some(statistics);
    this.error = Option.some(error);
  }

  public static AnalysisResult success(final CombinedStatistics statistics) {
    return new AnalysisResult(statistics, null);
  }

  public static AnalysisResult fail(final Exception error) {
    return new AnalysisResult(null, error);
  }

  public Option<CombinedStatistics> getStatistics() {
    return this.statistics;
  }

  public Option<Exception> getError() {
    return this.error;
  }

}
