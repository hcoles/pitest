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
package org.pitest.mutationtest;

import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.tooling.AnalysisResult;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.util.Unchecked;

/**
 * Entry point for command line interface
 */
public class MutationCoverageReport {

  public static void main(final String args[]) {

    final OptionsParser parser = new OptionsParser();
    final ParseResult pr = parser.parse(args);

    if (!pr.isOk()) {
      parser.printHelp();
      System.out.println(">>>> " + pr.getErrorMessage().value());
    } else {
      final ReportOptions data = pr.getOptions();
      final MutationStatistics stats = runReport(data);
      throwErrorIfScoreBelowThreshold(stats, data.getMutationThreshold());
    }

  }

  private static void throwErrorIfScoreBelowThreshold(
      final MutationStatistics stats, final int threshold) {
    if ((threshold != 0) && (stats.getPercentageDetected() < threshold)) {
      throw new RuntimeException("Mutation score of "
          + stats.getPercentageDetected() + " is below threshold of "
          + threshold);
    }
  }

  private static MutationStatistics runReport(final ReportOptions data) {

    final EntryPoint e = new EntryPoint();
    final AnalysisResult result = e.execute(null, data);
    if (result.getError().hasSome()) {
      throw Unchecked.translateCheckedException(result.getError().value());
    }
    return result.getStatistics().value();

  }

}
