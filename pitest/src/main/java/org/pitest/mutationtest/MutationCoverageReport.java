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

import java.io.Reader;

import org.pitest.classinfo.CodeSource;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.DefaultCoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.mutationtest.incremental.XStreamHistoryStore;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.mutationtest.statistics.MutationStatistics;

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
      MutationStatistics stats = runReport(data);      
      throwErrorIfScoreBelowThreshold(stats, data.getMutationThreshold());
    }

  }

  private static void throwErrorIfScoreBelowThreshold(MutationStatistics stats, int threshold) {
    if ( threshold != 0 && stats.getPercentageDetected() < threshold ) {
      throw new RuntimeException("Mutation score of " + stats.getPercentageDetected() + " is below threshold of " + threshold );
    }
  }

  private static MutationStatistics runReport(final ReportOptions data) {

    final SettingsFactory settings = new SettingsFactory(data);

    final JarCreatingJarFinder agent = new JarCreatingJarFinder(
        new ClassPathByteArraySource(data.getClassPath()));

    final ResultOutputStrategy outputStrategy = settings.getOutputStrategy();

    final Option<Reader> reader = data.createHistoryReader();
    final WriterFactory historyWriter = data.createHistoryWriter();

    try {

      final CompoundListenerFactory reportFactory = new CompoundListenerFactory(
          FCollection.map(data.getOutputFormats(),
              OutputFormat.createFactoryForFormat(outputStrategy)));

      final CoverageOptions coverageOptions = data.createCoverageOptions();
      final LaunchOptions launchOptions = new LaunchOptions(agent,
          data.getJvmArgs());
      final MutationClassPaths cps = data.getMutationClassPaths();
      final Timings timings = new Timings();

      final CodeSource code = new CodeSource(cps, coverageOptions
          .getPitConfig().testClassIdentifier());

      final CoverageGenerator coverageGenerator = new DefaultCoverageGenerator(
          null, coverageOptions, launchOptions, code,
          settings.createCoverageExporter(), timings, !data.isVerbose());

      final HistoryStore history = new XStreamHistoryStore(historyWriter,
          reader);

      final MutationStrategies strategies = new MutationStrategies(history,
          coverageGenerator, reportFactory);

      final MutationCoverage instance = new MutationCoverage(strategies, null,
          code, data, timings);

      return instance.run();
    } finally {
      agent.close();
      historyWriter.close();
    }
  }

}
