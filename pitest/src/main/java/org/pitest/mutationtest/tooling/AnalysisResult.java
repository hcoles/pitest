package org.pitest.mutationtest.tooling;

import org.pitest.functional.Option;
import org.pitest.mutationtest.statistics.MutationStatistics;

public class AnalysisResult {

  private final Option<MutationStatistics> statistics;
  private final Option<Exception>          error;

  private AnalysisResult(final MutationStatistics statistics,
      final Exception error) {
    this.statistics = Option.some(statistics);
    this.error = Option.some(error);
  }

  public static AnalysisResult success(final MutationStatistics statistics) {
    return new AnalysisResult(statistics, null);
  }

  public static AnalysisResult fail(final Exception error) {
    return new AnalysisResult(null, error);
  }

  public Option<MutationStatistics> getStatistics() {
    return this.statistics;
  }

  public Option<Exception> getError() {
    return this.error;
  }

}
