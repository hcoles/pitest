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

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import org.pitest.DefaultStaticConfig;
import org.pitest.Pitest;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.CodeSource;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.containers.BaseThreadPoolContainer;
import org.pitest.containers.UnContainer;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.Container;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.internal.IsolationUtils;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.filter.LimitNumberOfMutationPerClassFilter;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.mutationtest.filter.UnfilteredMutationFilter;
import org.pitest.mutationtest.incremental.DefaultCodeHistory;
import org.pitest.mutationtest.incremental.HistoryListener;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.incremental.IncrementalAnalyser;
import org.pitest.mutationtest.report.SmartSourceLocator;
import org.pitest.mutationtest.statistics.MutationStatisticsListener;
import org.pitest.mutationtest.statistics.Score;
import org.pitest.mutationtest.verify.BuildVerifier;
import org.pitest.util.Log;
import org.pitest.util.StringUtil;
import org.pitest.util.Unchecked;

public class MutationCoverage implements Runnable {

  private final static int        MB  = 1024 * 1024;

  private static final Logger     LOG = Log.getLogger();
  private final ReportOptions     data;
  private final ListenerFactory   listenerFactory;
  private final CoverageGenerator coverage;
  private final Timings           timings;
  private final BuildVerifier     buildVerifier;
  private final CodeSource        code;
  private final HistoryStore      historyStore;
  private final File              baseDir;

  public MutationCoverage(final File baseDir, final HistoryStore history,
      final CodeSource code, final CoverageGenerator coverage,
      final ReportOptions data, final ListenerFactory listenerFactory,
      final Timings timings, final BuildVerifier buildVerifier) {
    this.historyStore = history;
    this.coverage = coverage;
    this.listenerFactory = listenerFactory;
    this.data = data;
    this.timings = timings;
    this.buildVerifier = buildVerifier;
    this.code = code;
    this.baseDir = baseDir;
  }

  public final void run() {
    try {
      this.runReport();

    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  private void runReport() throws IOException {

    Log.setVerbose(this.data.isVerbose());

    final Runtime runtime = Runtime.getRuntime();

    LOG.fine("System class path is " + System.getProperty("java.class.path"));
    LOG.fine("Maxmium available memory is " + (runtime.maxMemory() / MB)
        + " mb");

    final long t0 = System.currentTimeMillis();

    verifyBuildSuitableForMutationTesting();

    final CoverageDatabase coverageData = this.coverage.calculateCoverage();

    LOG.fine("Used memory after coverage calculation "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory after coverage calculation "
        + (runtime.freeMemory() / MB) + " mb");

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final MutationResultListener mutationReportListener = this.listenerFactory
        .getListener(coverageData, t0,
            new SmartSourceLocator(this.data.getSourceDirs()));

    staticConfig.addTestListener(MutationResultAdapter
        .adapt(mutationReportListener));
    staticConfig.addTestListener(MutationResultAdapter
        .adapt(new HistoryListener(this.historyStore)));

    final MutationStatisticsListener stats = new MutationStatisticsListener();
    staticConfig.addTestListener(stats);

    this.historyStore.initialize();

    this.timings.registerStart(Timings.Stage.BUILD_MUTATION_TESTS);
    final List<TestUnit> tus = buildMutationTests(coverageData);
    this.timings.registerEnd(Timings.Stage.BUILD_MUTATION_TESTS);

    LOG.info("Created  " + tus.size() + " mutation test units");
    checkMutationsFound(tus);

    recordClassPath(coverageData);

    LOG.fine("Used memory before analysis start "
        + ((runtime.totalMemory() - runtime.freeMemory()) / MB) + " mb");
    LOG.fine("Free Memory before analysis start " + (runtime.freeMemory() / MB)
        + " mb");

    final Pitest pit = new Pitest(staticConfig);
    this.timings.registerStart(Timings.Stage.RUN_MUTATION_TESTS);
    pit.run(createContainer(), tus);
    this.timings.registerEnd(Timings.Stage.RUN_MUTATION_TESTS);

    LOG.info("Completed in " + timeSpan(t0));

    printStats(stats);

  }

  private void recordClassPath(final CoverageDatabase coverageData) {
    final Set<ClassName> allClassNames = getAllClassesAndTests(coverageData);
    final Collection<HierarchicalClassId> ids = FCollection.map(
        this.code.getClassInfo(allClassNames), ClassInfo.toFullClassId());
    this.historyStore.recordClassPath(ids);
  }

  private Set<ClassName> getAllClassesAndTests(
      final CoverageDatabase coverageData) {
    final Set<ClassName> names = new HashSet<ClassName>();
    for (final ClassName each : this.code.getCodeUnderTestNames()) {
      names.add(each);
      FCollection.mapTo(coverageData.getTestsForClass(each),
          TestInfo.toDefiningClassName(), names);
    }
    return names;
  }

  private void verifyBuildSuitableForMutationTesting() {
    this.buildVerifier.verify(this.code);
  }

  private void printStats(final MutationStatisticsListener stats) {
    final PrintStream ps = System.out;
    ps.println(StringUtil.seperatorLine('='));
    ps.println("- Timings");
    ps.println(StringUtil.seperatorLine('='));
    this.timings.report(ps);

    ps.println(StringUtil.seperatorLine('='));
    ps.println("- Statistics");
    ps.println(StringUtil.seperatorLine('='));
    stats.getStatistics().report(ps);

    ps.println(StringUtil.seperatorLine('='));
    ps.println("- Mutators");
    ps.println(StringUtil.seperatorLine('='));
    for (final Score each : stats.getStatistics().getScores()) {
      each.report(ps);
      ps.println(StringUtil.seperatorLine());
    }
  }

  private List<TestUnit> buildMutationTests(final CoverageDatabase coverageData) {
    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        this.data.isMutateStaticInitializers(),
        Prelude.or(this.data.getExcludedMethods()),
        this.data.getLoggingClasses(), this.data.getMutators(),
        this.data.isDetectInlinedCode());

    final MutationConfig mutationConfig = new MutationConfig(engine,
        this.data.getJvmArgs());

    final MutationSource source = new MutationSource(mutationConfig,
        limitMutationsPerClass(), coverageData, new ClassPathByteArraySource(
            this.data.getClassPath()));

    final MutationAnalyser analyser = new IncrementalAnalyser(
        new DefaultCodeHistory(this.code, this.historyStore), coverageData);

    final MutationTestBuilder builder = new MutationTestBuilder(this.baseDir,
        mutationConfig, analyser, source, this.data,
        this.coverage.getConfiguration(), this.coverage.getJavaAgent());

    return builder.createMutationTestUnits(this.code.getCodeUnderTestNames());
  }

  private void checkMutationsFound(final List<TestUnit> tus) {
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
