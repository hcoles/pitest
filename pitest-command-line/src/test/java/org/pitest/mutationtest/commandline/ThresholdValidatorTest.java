package org.pitest.mutationtest.commandline;

import org.junit.Test;
import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.statistics.Score;

import java.util.Collections;

import static org.junit.Assert.fail;

public class ThresholdValidatorTest {

  private final ThresholdValidator testee = new ThresholdValidator();

  @Test
  public void shouldThrowErrorIfScoreBelowCoverageThreshold() throws Exception {
    final CoverageSummary coverageSummary = new CoverageSummary(1, 0);

    try {
      this.testee.throwErrorIfScoreBelowCoverageThreshold(coverageSummary, 1);
      fail();
    } catch (RuntimeException e) {
    }
  }

  @Test
  public void shouldNotThrowErrorIfScoreBelowCoverageThresholdZero() throws Exception {
    final CoverageSummary coverageSummary = new CoverageSummary(1, 0);

    try {
      this.testee.throwErrorIfScoreBelowCoverageThreshold(coverageSummary, 0);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowErrorIfScoreAboveCoverage() throws Exception {
    final CoverageSummary coverageSummary = new CoverageSummary(2, 1);

    try {
      this.testee.throwErrorIfScoreBelowCoverageThreshold(coverageSummary, 49);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowErrorIfScoreEqualToCoverage() throws Exception {
    final CoverageSummary coverageSummary = new CoverageSummary(2, 1);

    try {
      this.testee.throwErrorIfScoreBelowCoverageThreshold(coverageSummary, 50);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldThrowErrorIfScoreBelowMutationThreshold() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 1, 0, 0);

    try {
      this.testee.throwErrorIfScoreBelowMutationThreshold(mutationStatistics, 1);
      fail();
    } catch (RuntimeException e) {
    }
  }

  @Test
  public void shouldNotThrowErrorIfScoreBelowMutationThresholdZero() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 1, 0, 0);

    try {
      this.testee.throwErrorIfScoreBelowMutationThreshold(mutationStatistics, 0);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowErrorIfScoreAboveMutationThreshold() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 4, 1, 0);

    try {
      this.testee.throwErrorIfScoreBelowMutationThreshold(mutationStatistics, 24);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowErrorIfScoreEqualToMutationThreshold() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 4, 1, 0);

    try {
      this.testee.throwErrorIfScoreBelowMutationThreshold(mutationStatistics, 25);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldThrowErrorIfMoreThanMaxSurvivingMutantsThresholdZero() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 1, 0, 0);

    try {
      this.testee.throwErrorIfMoreThanMaxSurvivingMutants(mutationStatistics, 0);
      fail();
    } catch (RuntimeException e) {
    }
  }

  @Test
  public void shouldNotThrowErrorIfEqualToMaxSurvivingMutantsThresholdZero() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 1, 0, 0);

    try {
      this.testee.throwErrorIfMoreThanMaxSurvivingMutants(mutationStatistics, 1);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowErrorIfLessThanMaxSurvivingMutantsThresholdOne() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 1, 1, 0);

    try {
      this.testee.throwErrorIfMoreThanMaxSurvivingMutants(mutationStatistics, 1);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldNotThrowErrorIfEqualToMaxSurvivingMutantsThresholdOne() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 2, 1, 0);

    try {
      this.testee.throwErrorIfMoreThanMaxSurvivingMutants(mutationStatistics, 1);
    } catch (RuntimeException e) {
      fail();
    }
  }

  @Test
  public void shouldThrowErrorIfMoreThanMaxSurvivingMutantsThresholdOne() throws Exception {
    final Iterable<Score> scores = Collections.emptyList();
    final MutationStatistics mutationStatistics = new MutationStatistics(scores, 1, 0, 0);

    try {
      this.testee.throwErrorIfMoreThanMaxSurvivingMutants(mutationStatistics, 1);
    } catch (RuntimeException e) {
      fail();
    }
  }

}
