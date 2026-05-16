/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.commandline;

import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.tooling.AnalysisResult;
import org.pitest.mutationtest.tooling.CombinedStatistics;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.util.Unchecked;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * Entry point for command line interface
 */
public class MutationCoverageReport {

  public static void main(final String[] args) {

    final PluginServices plugins = PluginServices.makeForContextLoader();
    final OptionsParser parser = new OptionsParser(new PluginFilter(plugins));
    final ParseResult pr = parser.parse(args);

    if (!pr.isOk()) {
      parser.printHelp();
      System.out.println(">>>> " + pr.getErrorMessage().get());
    } else {
      final ReportOptions data = pr.getOptions();

      final CombinedStatistics stats = runReport(data, plugins);

      int precision = data.getThresholdPrecision();
      throwErrorIfScoreBelowCoverageThreshold(stats.getCoverageSummary(),
          data.getCoverageThreshold(), precision);
      throwErrorIfScoreBelowTestStrengthThreshold(stats.getMutationStatistics(),
              data.getTestStrengthThreshold(), precision);
      throwErrorIfScoreBelowMutationThreshold(stats.getMutationStatistics(),
          data.getMutationThreshold(), precision);
      throwErrorIfMoreThanMaxSurvivingMutants(stats.getMutationStatistics(), data.getMaximumAllowedSurvivors());
    }

  }

  private static void throwErrorIfScoreBelowCoverageThreshold(
      CoverageSummary stats, BigDecimal threshold, int precision) {
    if (threshold.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal actual = stats.getCoverage(precision);
      if (actual.compareTo(threshold) < 0) {
        throw new RuntimeException("Line coverage of " + actual
            + " is below threshold of " + threshold);
      }
    }
  }

  private static void throwErrorIfScoreBelowMutationThreshold(
      final MutationStatistics stats, final BigDecimal threshold, int precision) {
    if (threshold.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal actual = stats.getPercentageDetected(precision);
      if (actual.compareTo(threshold) < 0) {
        throw new RuntimeException("Mutation score of "
            + actual + " is below threshold of "
            + threshold);
      }
    }
  }

  private static void throwErrorIfScoreBelowTestStrengthThreshold(
          final MutationStatistics stats, final BigDecimal threshold, int precision) {
    if (threshold.compareTo(BigDecimal.ZERO) != 0) {
      BigDecimal actual = stats.getTestStrength(precision);
      if (actual.compareTo(threshold) < 0) {
        throw new RuntimeException("Test strength score of "
                + actual + " is below threshold of "
                + threshold);
      }
    }
  }

  private static void throwErrorIfMoreThanMaxSurvivingMutants(
      final MutationStatistics stats, final long threshold) {
    if ((threshold >= 0)
        && (stats.getTotalSurvivingMutations() > threshold)) {
      throw new RuntimeException("Had "
          + stats.getTotalSurvivingMutations() + " surviving mutants, but only "
          + threshold + " survivors allowed");
    }
  }

  private static CombinedStatistics runReport(ReportOptions data,
      PluginServices plugins) {

    final EntryPoint e = new EntryPoint();
    final AnalysisResult result = e.execute(null, data, plugins,
        new HashMap<>());
    if (result.getError().isPresent()) {
      throw Unchecked.translateCheckedException(result.getError().get());
    }
    return result.getStatistics().get();

  }

}
