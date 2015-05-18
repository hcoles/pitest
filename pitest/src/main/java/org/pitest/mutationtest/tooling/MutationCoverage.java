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

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.ListenerArguments;
import org.pitest.mutationtest.MutationAnalyser;
import org.pitest.mutationtest.MutationConfig;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationGrouper;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.build.TestPrioritiser;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.execute.MutationAnalysisExecutor;
import org.pitest.mutationtest.filter.MutationFilter;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.incremental.DefaultCodeHistory;
import org.pitest.mutationtest.incremental.HistoryListener;
import org.pitest.mutationtest.incremental.IncrementalAnalyser;
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
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Logger;

public class MutationCoverage {

  private static final Logger        LOG = Log.getLogger();
  private static final int           MB  = 1024 * 1024;

  private final ReportOptions        data;
  private final MutationStrategies   strategies;
  private final Timings              timings;
  private final CodeSource           code;
  private final File                 baseDir;
  private final SettingsFactory      settings;

  public MutationCoverage(MutationStrategies strategies,
                          File baseDir,
                          CodeSource code,
                          ReportOptions data,
                          SettingsFactory settings,
                          Timings timings) {
    this.strategies = strategies;
    this.data = data;
    this.settings = settings;
    this.timings = timings;
    this.code = code;
    this.baseDir = baseDir;
  }

  public CombinedStatistics runReport() throws IOException {

    Log.setVerbose(data.isVerbose());

    Runtime runtime = Runtime.getRuntime();

    if (!data.isVerbose()) {
      LOG.info("Verbose logging is disabled. If you encounter an problem please enable it before reporting an issue.");
    }

    LOG.fine("Running report with " + data);

    LOG.fine("System class path is " + System.getProperty("java.class.path"));
    LOG.fine("Maximum available memory is " + (runtime.maxMemory() / MB) + " mb");

    long startTime = System.currentTimeMillis();

    verifyBuildSuitableForMutationTesting();

    CoverageDatabase coverageData = coverage().calculateCoverage();

    LOG.fine("Used memory after coverage calculation "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory after coverage calculation "
        + (runtime.freeMemory() / MB) + " mb");

    MutationStatisticsListener stats = new MutationStatisticsListener();

    MutationEngine engine = strategies.factory().createEngine(
        data.isMutateStaticInitializers(),
        Prelude.or(data.getExcludedMethods()),
        data.getLoggingClasses(), data.getMutators(),
        data.isDetectInlinedCode());

    List<MutationResultListener> config = createConfig(startTime, coverageData, stats, engine);

    history().initialize();

    timings.registerStart(Timings.Stage.BUILD_MUTATION_TESTS);
    List<MutationAnalysisUnit> tus = buildMutationTests(coverageData, engine);
    timings.registerEnd(Timings.Stage.BUILD_MUTATION_TESTS);

    LOG.info("Created  " + tus.size() + " mutation test units");
    checkMutationsFound(tus);

    recordClassPath(coverageData);

    LOG.fine("Used memory before analysis start "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory before analysis start " + (runtime.freeMemory() / MB) + " mb");

    MutationAnalysisExecutor mutationAnalysisExecutor = new MutationAnalysisExecutor(numberOfThreads(),config);
    timings.registerStart(Timings.Stage.RUN_MUTATION_TESTS);
    mutationAnalysisExecutor.run(tus);
    timings.registerEnd(Timings.Stage.RUN_MUTATION_TESTS);

    LOG.info("Completed in " + timeSpan(startTime));

    printStats(stats);

    return new CombinedStatistics(stats.getStatistics(), coverageData.createSummary());
  }

  private int numberOfThreads() {
    return Math.max(1, data.getNumberOfThreads());
  }

  private List<MutationResultListener> createConfig(long startTime,
                                                    CoverageDatabase coverageData,
                                                    MutationStatisticsListener stats,
                                                    MutationEngine engine) {
    List<MutationResultListener> ls = new ArrayList<MutationResultListener>();

    ls.add(stats);

    ListenerArguments args = new ListenerArguments(strategies.output(),
                                                   coverageData,
                                                   new SmartSourceLocator(data.getSourceDirs()),
                                                   engine,
                                                   startTime);

    MutationResultListener mutationReportListener = strategies.listenerFactory()
        .getListener(data.getFreeFormProperties(), args);

    ls.add(mutationReportListener);
    ls.add(new HistoryListener(history()));

    if (!data.isVerbose()) {
      ls.add(new SpinnerListener(System.out));
    }
    return ls;
  }

  private void recordClassPath(CoverageDatabase coverageData) {
    Set<ClassName> allClassNames = getAllClassesAndTests(coverageData);
    Collection<ClassInfo> classInfo = code.getClassInfo(allClassNames);
    Collection<HierarchicalClassId> ids = FCollection.map(classInfo, ClassInfo.toFullClassId());
    history().recordClassPath(ids, coverageData);
  }

  private Set<ClassName> getAllClassesAndTests(CoverageDatabase coverageData) {
    Set<ClassName> names = new HashSet<ClassName>();
    for (ClassName each : code.getCodeUnderTestNames()) {
      names.add(each);
      FCollection.mapTo(coverageData.getTestsForClass(each),
                        TestInfo.toDefiningClassName(),
                        names);
    }
    return names;
  }

  private void verifyBuildSuitableForMutationTesting() {
    strategies.buildVerifier().verify(code);
  }

  private void printStats(MutationStatisticsListener stats) {
    PrintStream ps = System.out;
    ps.println(StringUtil.separatorLine('='));
    ps.println("- Timings");
    ps.println(StringUtil.separatorLine('='));
    timings.report(ps);

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Statistics");
    ps.println(StringUtil.separatorLine('='));
    stats.getStatistics().report(ps);

    ps.println(StringUtil.separatorLine('='));
    ps.println("- Mutators");
    ps.println(StringUtil.separatorLine('='));
    for (Score each : stats.getStatistics().getScores()) {
      each.report(ps);
      ps.println(StringUtil.separatorLine());
    }
  }

  private List<MutationAnalysisUnit> buildMutationTests( CoverageDatabase coverageData,
                                                         MutationEngine engine) {

    MutationConfig mutationConfig = new MutationConfig(engine, coverage().getLaunchOptions());

    ClassByteArraySource bas = new ClassPathByteArraySource(data.getClassPath());

    Properties freeFormProperties = data.getFreeFormProperties();
    TestPrioritiser testPrioritiser = settings.getTestPrioritiser()
                                              .makeTestPrioritiser( freeFormProperties,
                                                                    code,
                                                                    coverageData);

    MutationFilter filter = makeFilter().createFilter(freeFormProperties, code,
                                                      data.getMaxMutationsPerClass());
    MutationSource source = new MutationSource(mutationConfig, filter, testPrioritiser, bas);

    MutationAnalyser analyser = new IncrementalAnalyser(new DefaultCodeHistory(code, history()), coverageData);

    PercentAndConstantTimeoutStrategy timeoutStrategy = new PercentAndConstantTimeoutStrategy(data.getTimeoutFactor(),
                                                                                              data.getTimeoutConstant());
    WorkerFactory wf = new WorkerFactory(baseDir, coverage().getConfiguration(),
                                         mutationConfig,
                                         timeoutStrategy,
                                         data.isVerbose(),
                                         data.getClassPath().getLocalClassPath());

    MutationGrouper grouper = settings.getMutationGrouper().makeFactory(freeFormProperties,
                                                                        code,
                                                                        data.getNumberOfThreads(),
                                                                        data.getMutationUnitSize());
    MutationTestBuilder builder = new MutationTestBuilder(wf, analyser, source, grouper);

    return builder.createMutationTestUnits(code.getCodeUnderTestNames());
  }

  private MutationFilterFactory makeFilter() {
    return settings.createMutationFilter();
  }

  private void checkMutationsFound(List<MutationAnalysisUnit> tus) {
    if (tus.isEmpty()) {
      if (data.shouldFailWhenNoMutations()) {
        throw new PitHelpError(Help.NO_MUTATIONS_FOUND);
      } else {
        LOG.warning(Help.NO_MUTATIONS_FOUND.toString());
      }
    }
  }


  private String timeSpan(long t0) {
    return "" + ((System.currentTimeMillis() - t0) / 1000) + " seconds";
  }

  private CoverageGenerator coverage() {
    return strategies.coverage();
  }

  private HistoryStore history() {
    return strategies.history();
  }
}
