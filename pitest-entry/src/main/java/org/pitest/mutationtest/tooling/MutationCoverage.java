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
package org.pitest.mutationtest.tooling;

import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.CoverageSummary;
import org.pitest.coverage.NoCoverage;
import org.pitest.coverage.ReportCoverage;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultInterceptor;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationGrouper;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.build.TestPrioritiser;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.execute.MutationAnalysisExecutor;
import org.pitest.mutationtest.incremental.HistoryListener;
import org.pitest.mutationtest.incremental.NullHistory;
import org.pitest.mutationtest.statistics.MutationStatistics;
import org.pitest.mutationtest.statistics.MutationStatisticsListener;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.util.Log;
import org.pitest.util.StringUtil;
import org.pitest.util.Timings;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class MutationCoverage {

  private static final int         MB  = 1024 * 1024;

  private static final Logger      LOG = Log.getLogger();
  private final ReportOptions      data;

  private final MutationStrategies strategies;
  private final Timings            timings;
  private final CodeSource         code;
  private final File               baseDir;
  private final SettingsFactory    settings;

  public MutationCoverage(final MutationStrategies strategies,
      final File baseDir, final CodeSource code, final ReportOptions data,
      final SettingsFactory settings, final Timings timings) {
    this.strategies = strategies;
    this.data = data;
    this.settings = settings;
    this.timings = timings;
    this.code = code;
    this.baseDir = baseDir;
  }

  public CombinedStatistics runReport() throws IOException {

    if (!this.data.getVerbosity().showMinionOutput()) {
      LOG.info("Verbose logging is disabled. If you encounter a problem, please enable it before reporting an issue.");
    }
    Log.setVerbose(this.data.getVerbosity());

    final Runtime runtime = Runtime.getRuntime();

    LOG.fine("Running report with " + this.data);

    LOG.fine("System class path is " + System.getProperty("java.class.path"));
    LOG.fine("Maximum available memory is " + (runtime.maxMemory() / MB)
        + " mb");

    final long t0 = System.nanoTime();

    List<String> issues = verifyBuildSuitableForMutationTesting();

    checkExcludedRunners();

    final EngineArguments args = EngineArguments.arguments()
            .withExcludedMethods(this.data.getExcludedMethods())
            .withMutators(this.data.getMutators());
    final MutationEngine engine = this.strategies.factory().createEngine(args);

    List<MutationAnalysisUnit> preScanMutations = findMutations(engine, args);
    LOG.info("Created " + preScanMutations.size() + " mutation test units in pre scan");

    // throw error if configured to do so
    checkMutationsFound(preScanMutations);

    if (preScanMutations.isEmpty()) {
      LOG.info("Skipping coverage and analysis as no mutations found" );
      return emptyStatistics();
    }

    return runAnalysis(runtime, t0, args, engine, issues);

  }

  private CombinedStatistics emptyStatistics() {
    MutationStatistics mutationStatistics = new MutationStatistics(emptyList(),0,0,0,0, emptySet());
    return new CombinedStatistics(mutationStatistics, new CoverageSummary(0,0), Collections.emptyList());
  }

  private CombinedStatistics runAnalysis(Runtime runtime, long t0, EngineArguments args, MutationEngine engine, List<String> issues) {
    History history = this.strategies.history();
    history.initialize();

    CoverageDatabase coverageData = coverage().calculateCoverage(history.limitTests());
    history.processCoverage(coverageData);

    LOG.fine("Used memory after coverage calculation "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory after coverage calculation "
        + (runtime.freeMemory() / MB) + " mb");

    final MutationStatisticsListener stats = new MutationStatisticsListener();

    this.timings.registerStart(Timings.Stage.BUILD_MUTATION_TESTS);
    final List<MutationAnalysisUnit> tus = buildMutationTests(coverageData, history,
            engine, args, allInterceptors());
    this.timings.registerEnd(Timings.Stage.BUILD_MUTATION_TESTS);

    LOG.info("Created " + tus.size() + " mutation test units" );

    LOG.fine("Used memory before analysis start "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory before analysis start " + (runtime.freeMemory() / MB)
        + " mb");

    ReportCoverage modifiedCoverage = transformCoverage(coverageData);
    final List<MutationResultListener> config = createConfig(t0, modifiedCoverage, history,
                stats, engine);
    final MutationAnalysisExecutor mae = new MutationAnalysisExecutor(
        numberOfThreads(), resultInterceptor(), config);
    this.timings.registerStart(Timings.Stage.RUN_MUTATION_TESTS);
    mae.run(tus);
    this.timings.registerEnd(Timings.Stage.RUN_MUTATION_TESTS);

    LOG.info("Completed in " + timeSpan(t0));

    MutationStatistics mutationStats = stats.getStatistics();
    CombinedStatistics combined = new CombinedStatistics(mutationStats,
            createSummary(modifiedCoverage, mutationStats.mutatedClasses()), issues);

    printStats(combined);

    return combined;
  }

  private ReportCoverage transformCoverage(ReportCoverage coverageData) {
    // cosmetic changes to coverage are made only after tests are assigned to
    // mutants to ensure they cannot affect results.
    return strategies.coverageTransformer().transform(coverageData);
  }

  private CoverageSummary createSummary(ReportCoverage modifiedCoverage, Set<ClassName> mutatedClasses) {
    List<ClassName> examinedClasses = this.code.getCodeUnderTestNames().stream()
            .filter(mutatedClasses::contains)
            .collect(Collectors.toList());

    int numberOfCodeLines = examinedClasses.stream()
            .map(c -> modifiedCoverage.getCodeLinesForClass(c))
            .map(c -> c.getNumberOfCodeLines())
            .reduce(0, Integer::sum);

    int coveredLines = examinedClasses.stream()
            .mapToInt(c -> modifiedCoverage.getCoveredLines(c).size())
            .sum();

    return new CoverageSummary(numberOfCodeLines, coveredLines);
  }

  private Predicate<MutationInterceptor> allInterceptors() {
    return i -> true;
  }

  private List<MutationAnalysisUnit> findMutations(MutationEngine engine, EngineArguments args) {
    // Run mutant discovery without coverage data or history.
    // Ideally we'd ony discover mutants once, but the process is currently tightly
    // entangled with coverage data. Generating coverage data is expensive for
    // some projects, while discovery usually takes less than 1 second. By doing
    // an initial run here we are able to skip coverage generation when no mutants
    // are found, e.g if pitest is being run against diffs.
    this.timings.registerStart(Timings.Stage.MUTATION_PRE_SCAN);
    List<MutationAnalysisUnit> mutants = buildMutationTests(new NoCoverage(), new NullHistory(), engine, args, noReportsOrFilters());
    this.timings.registerEnd(Timings.Stage.MUTATION_PRE_SCAN);
    return mutants;
  }

  private Predicate<MutationInterceptor> noReportsOrFilters() {
    return i -> i.type().includeInPrescan();
  }


  private void checkExcludedRunners() {
    final Collection<String> excludedRunners = this.data.getExcludedRunners();
    if (!excludedRunners.isEmpty()) {
      // Check whether JUnit4 is available or not
      try {
        Class.forName("org.junit.runner.RunWith");
      } catch (final ClassNotFoundException e) {
        // JUnit4 is not available on the classpath
        throw new PitHelpError(Help.NO_JUNIT_EXCLUDE_RUNNERS);
      }
    }
  }

  private int numberOfThreads() {
    return Math.max(1, this.data.getNumberOfThreads());
  }

  private List<MutationResultListener> createConfig(long t0,
                                                    ReportCoverage coverageData,
                                                    History history,
                                                    MutationStatisticsListener stats,
                                                    MutationEngine engine) {
    final List<MutationResultListener> ls = new ArrayList<>();

    ls.add(stats);

    final ListenerArguments args = new ListenerArguments(
        this.strategies.output(), coverageData, new SmartSourceLocator(
            data.getSourcePaths(), this.data.getInputEncoding()), engine, t0, this.data.isFullMutationMatrix(), data);

    final MutationResultListener mutationReportListener = this.strategies
        .listenerFactory().getListener(this.data.getFreeFormProperties(), args);

    ls.add(mutationReportListener);
    ls.add(new HistoryListener(history));

    if (this.data.getVerbosity().showSpinner()) {
      ls.add(new SpinnerListener(System.out));
    }
    return ls;
  }

  private MutationResultInterceptor resultInterceptor() {
    return this.strategies.resultInterceptor();
  }

  private List<String> verifyBuildSuitableForMutationTesting() {
    return this.strategies.buildVerifier().verify();
  }

  private void printStats(CombinedStatistics combinedStatistics) {
    MutationStatistics stats = combinedStatistics.getMutationStatistics();
    final PrintStream ps = System.out;

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Mutators");
    ps.println(StringUtil.separatorLine('='));
    for (final Score each : stats.getScores()) {
      each.report(ps);
      ps.println(StringUtil.separatorLine());
    }

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Timings");
    ps.println(StringUtil.separatorLine('='));
    this.timings.report(ps);

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Statistics");
    ps.println(StringUtil.separatorLine('='));

    final CoverageSummary coverage = combinedStatistics.getCoverageSummary();
    if (coverage != null) {
      ps.println(String.format(">> Line Coverage (for mutated classes only): %d/%d (%d%%)", coverage.getNumberOfCoveredLines(),
              coverage.getNumberOfLines(), coverage.getCoverage()));
    }

    stats.report(ps);

    if (!combinedStatistics.getIssues().isEmpty()) {
      ps.println();
      ps.println("!! The following issues were detected during the run !!");
      combinedStatistics.getIssues().forEach(ps::println);
    }
  }

  private List<MutationAnalysisUnit> buildMutationTests(CoverageDatabase coverageData,
                                                        History history,
                                                        MutationEngine engine,
                                                        EngineArguments args,
                                                        Predicate<MutationInterceptor> interceptorFilter) {

    final MutationConfig mutationConfig = new MutationConfig(engine, coverage()
        .getLaunchOptions());

    final ClassByteArraySource bas = new CachingByteArraySource(fallbackToClassLoader(new ClassPathByteArraySource(
        this.data.getClassPath())), 200);

    final TestPrioritiser testPrioritiser = this.settings.getTestPrioritiser()
        .makeTestPrioritiser(this.data.getFreeFormProperties(), this.code,
            coverageData);

    final MutationInterceptor interceptor = this.settings.getInterceptor()
            .createInterceptor(this.data, coverageData, bas, testPrioritiser)
            .filter(interceptorFilter);

    interceptor.initialise(this.code);

    final MutationSource source = new MutationSource(mutationConfig, testPrioritiser, bas, interceptor);


    final WorkerFactory wf = new WorkerFactory(this.baseDir, coverage()
        .getConfiguration(), mutationConfig, args,
        new PercentAndConstantTimeoutStrategy(this.data.getTimeoutFactor(),
            this.data.getTimeoutConstant()), this.data.getVerbosity(), this.data.isFullMutationMatrix(),
            this.data.getClassPath().getLocalClassPath());

    final MutationGrouper grouper = this.settings.getMutationGrouper().makeFactory(
        this.data.getFreeFormProperties(), this.code,
        this.data.getNumberOfThreads(), this.data.getMutationUnitSize());
    final MutationTestBuilder builder = new MutationTestBuilder(wf, history,
        source, grouper);

    return builder.createMutationTestUnits(this.code.getCodeUnderTestNames());
  }
  private void checkMutationsFound(final List<MutationAnalysisUnit> tus) {
    if (tus.isEmpty()) {
      if (this.data.shouldFailWhenNoMutations()) {
        throw new PitHelpError(Help.NO_MUTATIONS_FOUND);
      } else {
        LOG.warning(Help.NO_MUTATIONS_FOUND.toString());
      }
    }
  }

  private String timeSpan(final long t0) {
    return "" + (NANOSECONDS.toSeconds(System.nanoTime() - t0)) + " seconds";
  }

  private CoverageGenerator coverage() {
    return this.strategies.coverage();
  }

  // For reasons not yet understood classes from rt.jar are not resolved for some
  // projects during static analysis phase. For now fall back to the classloader when
  // a class not provided by project classpath
  private ClassByteArraySource fallbackToClassLoader(final ClassByteArraySource bas) {
    final ClassByteArraySource clSource = ClassloaderByteArraySource.fromContext();
    return clazz -> {
      final Optional<byte[]> maybeBytes = bas.getBytes(clazz);
      if (maybeBytes.isPresent()) {
        return maybeBytes;
      }
      LOG.log(Level.FINE, "Could not find " + clazz + " on classpath for analysis. Falling back to classloader");
      return clSource.getBytes(clazz);
    };
  }
}
