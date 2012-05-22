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

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.containers.BaseThreadPoolContainer;
import org.pitest.containers.UnContainer;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.DefaultCoverageDatabase;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.commandline.OptionsParser;
import org.pitest.mutationtest.commandline.ParseResult;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.filter.LimitNumberOfMutationPerClassFilter;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.filter.UnfilteredMutationFilter;
import org.pitest.mutationtest.instrument.JarCreatingJarFinder;
import org.pitest.mutationtest.report.DatedDirectoryResultOutputStrategy;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.mutationtest.report.SmartSourceLocator;
import org.pitest.mutationtest.statistics.MutationStatisticsListener;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.mutationtest.verify.BuildVerifier;
import org.pitest.mutationtest.verify.DefaultBuildVerifier;
import org.pitest.util.Log;
import org.pitest.util.StringUtil;
import org.pitest.util.Unchecked;

public class MutationCoverageReport implements Runnable {

  private static final Logger    LOG = Log.getLogger();
  private final ReportOptions    data;
  private final ListenerFactory  listenerFactory;
  private final CoverageDatabase coverageDatabase;
  private final Timings          timings;
  private final BuildVerifier    buildVerifier;

  public MutationCoverageReport(final CoverageDatabase coverageDatabase,
      final ReportOptions data, final ListenerFactory listenerFactory,
      final Timings timings, final BuildVerifier buildVerifier) {
    this.coverageDatabase = coverageDatabase;
    this.listenerFactory = listenerFactory;
    this.data = data;
    this.timings = timings;
    this.buildVerifier = buildVerifier;
  }

  public final void run() {
    try {
      this.runReport();

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  public static void main(final String args[]) {

    final OptionsParser parser = new OptionsParser();
    final ParseResult pr = parser.parse(args);

    if (!pr.isOk()) {
      parser.printHelp();
      System.out.println(">>>> " + pr.getErrorMessage().value());
    } else {
      final ReportOptions data = pr.getOptions();
      runReport(data);
    }

  }

  private static void runReport(final ReportOptions data) {
    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    try {

      final DatedDirectoryResultOutputStrategy outputStrategy = new DatedDirectoryResultOutputStrategy(
          data.getReportDir());
      final CompoundListenerFactory reportFactory = new CompoundListenerFactory(
          FCollection.map(data.getOutputFormats(),
              OutputFormat.createFactoryForFormat(outputStrategy)));

      final CoverageOptions coverageOptions = data.createCoverageOptions();
      final LaunchOptions launchOptions = new LaunchOptions(agent,
          data.getJvmArgs());
      final MutationClassPaths cps = data.getMutationClassPaths();
      final Timings timings = new Timings();

      final CoverageDatabase coverageDatabase = new DefaultCoverageDatabase(
          coverageOptions, launchOptions, cps, timings);

      final MutationCoverageReport instance = new MutationCoverageReport(
          coverageDatabase, data, reportFactory, timings,
          new DefaultBuildVerifier());

      instance.run();
    } finally {
      agent.close();
    }
  }

  private void runReport() throws IOException {

    Log.setVerbose(this.data.isVerbose());

    LOG.fine("System class path is " + System.getProperty("java.class.path"));

    final long t0 = System.currentTimeMillis();

    verifyBuildSuitableForMutationTesting();

    this.coverageDatabase.initialise();

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final TestListener mutationReportListener = this.listenerFactory
        .getListener(this.coverageDatabase, t0, new SmartSourceLocator(
            this.data.getSourceDirs()));

    staticConfig.addTestListener(mutationReportListener);

    final MutationStatisticsListener stats = new MutationStatisticsListener();
    staticConfig.addTestListener(stats);

    this.timings.registerStart(Timings.Stage.BUILD_MUTATION_TESTS);
    final List<TestUnit> tus = buildMutationTests();
    this.timings.registerEnd(Timings.Stage.BUILD_MUTATION_TESTS);

    LOG.info("Created  " + tus.size() + " mutation test units");
    checkMutationsFounds(tus);

    final Pitest pit = new Pitest(staticConfig);
    this.timings.registerStart(Timings.Stage.RUN_MUTATION_TESTS);
    pit.run(createContainer(), tus);
    this.timings.registerEnd(Timings.Stage.RUN_MUTATION_TESTS);

    LOG.info("Completed in " + timeSpan(t0));

    printStats(stats);

  }

  private void verifyBuildSuitableForMutationTesting() {
    this.buildVerifier.verify(this.coverageDatabase);
  }

  private void printStats(final MutationStatisticsListener stats) {
    System.out.println(StringUtil.seperatorLine('='));
    System.out.println("- Timings");
    System.out.println(StringUtil.seperatorLine('='));
    this.timings.report(System.out);

    System.out.println(StringUtil.seperatorLine('='));
    System.out.println("- Statistics");
    System.out.println(StringUtil.seperatorLine('='));
    stats.getStatistics().report(System.out);

    System.out.println(StringUtil.seperatorLine('='));
    System.out.println("- Mutators");
    System.out.println(StringUtil.seperatorLine('='));
    for (final Score each : stats.getStatistics().getScores()) {
      each.report(System.out);
      System.out.println(StringUtil.seperatorLine());
    }
  }

  private List<TestUnit> buildMutationTests() {
    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        this.data.isMutateStaticInitializers(),
        Prelude.or(this.data.getExcludedMethods()),
        this.data.getLoggingClasses(), this.data.getMutators());

    final MutationConfig mutationConfig = new MutationConfig(engine,
        this.data.getJvmArgs());
    final MutationTestBuilder builder = new MutationTestBuilder(mutationConfig,
        limitMutationsPerClass(), this.coverageDatabase, this.data,
        new ClassPathByteArraySource(this.data.getClassPath()));

    final Set<ClassName> codeClasses = new HashSet<ClassName>();
    FCollection.mapTo(this.coverageDatabase.getCodeClasses(),
        ClassInfo.toClassName(), codeClasses);
    final List<TestUnit> tus = builder.createMutationTestUnits(codeClasses);
    return tus;
  }

  private void checkMutationsFounds(final List<TestUnit> tus) {
    if (tus.isEmpty()) {
      if (this.data.shouldFailWhenNoMutations()) {
        throw new PitHelpError(Help.NO_MUTATIONS_FOUND);
      } else {
        LOG.warning(Help.NO_MUTATIONS_FOUND.toString());
      }
    }
  }

  private MutationFilterFactory limitMutationsPerClass() {
    if (this.data.getMaxMutationsPerClass() <= 0) {
      return UnfilteredMutationFilter.factory();
    } else {
      return LimitNumberOfMutationPerClassFilter.factory(this.data
          .getMaxMutationsPerClass());
    }

  }

  private Container createContainer() {
    if (this.data.getNumberOfThreads() > 1) {
      return new BaseThreadPoolContainer(this.data.getNumberOfThreads(),
          classLoaderFactory(), Executors.defaultThreadFactory()) {

      };
    } else {
      return new UnContainer();
    }
  }

  private ClassLoaderFactory classLoaderFactory() {
    final ClassLoader loader = IsolationUtils.getContextClassLoader();
    return new ClassLoaderFactory() {

      public ClassLoader get() {
        return loader;
      }

    };
  }

  private String timeSpan(final long t0) {
    return "" + ((System.currentTimeMillis() - t0) / 1000) + " seconds";
  }

}
