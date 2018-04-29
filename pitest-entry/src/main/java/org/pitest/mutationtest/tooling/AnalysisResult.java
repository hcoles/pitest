package org.pitest.mutationtest.tooling;

import java.util.Optional;

public final class AnalysisResult {

  private final Optional<CombinedStatistics> statistics;
  private final Optional<Exception>          error;

  private AnalysisResult(final CombinedStatistics statistics,
      final Exception error) {
    this.statistics = Optional.ofNullable(statistics);
    this.error = Optional.ofNullable(error);
  }

  public static AnalysisResult success(final CombinedStatistics statistics) {
    return new AnalysisResult(statistics, null);
  }

  public static AnalysisResult fail(final Exception error) {
    return new AnalysisResult(null, error);
  }

  public Optional<CombinedStatistics> getStatistics() {
    return this.statistics;
  }

  public Optional<Exception> getError() {
    return this.error;
  }

}
