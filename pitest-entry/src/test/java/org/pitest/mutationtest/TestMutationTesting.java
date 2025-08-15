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

import static java.util.function.Predicate.isEqual;
import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static org.pitest.mutationtest.DetectionStatus.MEMORY_ERROR;
import static org.pitest.mutationtest.DetectionStatus.NON_VIABLE;
import static org.pitest.mutationtest.DetectionStatus.NOT_STARTED;
import static org.pitest.mutationtest.DetectionStatus.NO_COVERAGE;
import static org.pitest.mutationtest.DetectionStatus.SURVIVED;
import static org.pitest.mutationtest.DetectionStatus.TIMED_OUT;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import com.example.systemtest.EatsMemoryWhenMutated;
import com.example.systemtest.InfiniteLoop;
import com.example.systemtest.NoMutations;
import com.example.systemtest.NoMutationsTest;
import com.example.systemtest.NoTests;
import com.example.systemtest.OneMutationFullTest;
import com.example.systemtest.OneMutationFullTestWithSystemPropertyDependency;
import com.example.systemtest.OneMutationOnly;
import com.example.systemtest.ThreeMutations;
import com.example.systemtest.ThreeMutationsTwoMeaningfullTests;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.pitest.SystemTest;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.DefaultCodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.NoTestStats;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.CompoundMutationInterceptor;
import org.pitest.mutationtest.build.DefaultGrouper;
import org.pitest.mutationtest.build.DefaultTestPrioritiser;
import org.pitest.mutationtest.build.MutationAnalysisUnit;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationSource;
import org.pitest.mutationtest.build.MutationTestBuilder;
import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.build.WorkerFactory;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.mutationtest.config.ExecutionMode;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.execute.MutationAnalysisExecutor;
import org.pitest.mutationtest.incremental.NullHistory;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.simpletest.TestAnnotationForTesting;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Timings;

import com.example.MutationsInNestedClasses;
import com.example.MutationsInNestedClassesTest;
import org.pitest.util.Verbosity;

@Category(SystemTest.class)
public class TestMutationTesting {

  private MutationAnalysisExecutor mae;
  private TestPluginArguments      config;

  private MetaDataExtractor        metaDataExtractor;

  @Before
  public void setUp() {
    this.config = TestPluginArguments.defaults();
    this.metaDataExtractor = new MetaDataExtractor();
    this.mae = new MutationAnalysisExecutor(1, result -> result,
        Collections
            .<MutationResultListener> singletonList(this.metaDataExtractor));
  }

  @Test
  public void shouldKillAllCoveredMutations() {
    run(OneMutationOnly.class, OneMutationFullTest.class,
        "PRIMITIVE_RETURNS");
    verifyResults(KILLED);
  }

  @Test
  public void shouldDetectedMixOfSurvivingAndKilledMutations() {
    run(ThreeMutations.class, ThreeMutationsTwoMeaningfullTests.class,
        "PRIMITIVE_RETURNS");
    verifyResults(SURVIVED, KILLED, KILLED);
  }

  @Test
  public void shouldReportNoResultsIfNoMutationsPossible() {
    run(NoMutations.class, NoMutationsTest.class, "PRIMITIVE_RETURNS");
    verifyResults();
  }

  @Test
  public void shouldReportStatusOfNoCoverageWhenNoTestsAvailable() {
    run(ThreeMutations.class, NoTests.class, "PRIMITIVE_RETURNS");
    verifyResults(NO_COVERAGE, NO_COVERAGE, NO_COVERAGE);
  }

  public static class InfiniteLoopTest {
    @TestAnnotationForTesting()
    public void pass() {
      assertEquals(3, InfiniteLoop.loop());
    }
  }

  @Test(timeout = 30000)
  public void shouldDetectAndEscapeFromInfiniteLoopsCausedByMutations() {
    run(InfiniteLoop.class, InfiniteLoopTest.class,
        "INCREMENTS");
    verifyResults(KILLED, TIMED_OUT);
  }

  @Test
  public void shouldExportSystemPropertiesToMinionProcess() {
    // System.setProperty("foo", "foo");
    // note surefire is configured to launch this test with -Dfoo=foo
    run(OneMutationOnly.class,
        OneMutationFullTestWithSystemPropertyDependency.class,
        "PRIMITIVE_RETURNS");
    verifyResults(KILLED);
  }

  public static class UnviableMutationsTest {
    @TestAnnotationForTesting
    public void test() {
      new OneMutationOnly();
      OneMutationOnly.returnOne();
    }
  }

  @Test
  @Ignore("no longer possible to serialize arbritrary mutators to child")
  public void shouldDetectUnviableMutations() {
    run(OneMutationOnly.class, UnviableMutationsTest.class,
        "UNVIABLE_CLASS_MUTATOR");
    verifyResults(NON_VIABLE, NON_VIABLE);

  }

  public static class EatsMemoryTest {
    @TestAnnotationForTesting()
    public void pass() throws InterruptedException {
      assertEquals(3, EatsMemoryWhenMutated.loop());
    }
  }

  @Ignore("flakey")
  @Test(timeout = 30000)
  public void shouldRecoverFromOutOfMemoryError() {
    run(EatsMemoryWhenMutated.class, EatsMemoryTest.class,
        "INCREMENTS");
    verifyResults(KILLED, MEMORY_ERROR);
  }

  @Test
  public void shouldIsolateMutationsFromNestedClasses() {
    // see http://code.google.com/p/pitestrunner/issues/detail?id=17 for full
    // description of this issue
    run(MutationsInNestedClasses.class, MutationsInNestedClassesTest.class,
        "FALSE_RETURNS");
    verifyResults(SURVIVED, SURVIVED);
  }

  @Test
  @Ignore("too brittle")
  public void shouldRecordCorrectLineNumberForMutations() {
    run(OneMutationOnly.class, OneMutationFullTest.class,
        "PRIMITIVE_RETURNS");
    verifyLineNumbers(111);
  }

  @Test
  public void dryRunModeMarksMutantsAsNotStarted() {
    run(OneMutationOnly.class, OneMutationFullTest.class, ExecutionMode.DRY_RUN,
            "PRIMITIVE_RETURNS");
    verifyResults(NOT_STARTED);
  }

  private void run(Class<?> clazz, Class<?> test,
                   final String ... mutators) {
    run (clazz, test,ExecutionMode.NORMAL, mutators);
  }

  private void run(Class<?> clazz, Class<?> test, ExecutionMode mode,
      final String ... mutators) {

    final ReportOptions data = new ReportOptions();

    final Set<Predicate<String>> tests = Collections.singleton(isEqual(test.getName()));
    data.setTargetTests(tests);

    final Set<String> mutees = Collections.singleton(clazz.getName() + "*");
    data.setTargetClasses(mutees);

    data.setTimeoutConstant(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT);
    data.setTimeoutFactor(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR);

    final JavaAgent agent = new JarCreatingJarFinder();

    try {
      createEngineAndRun(data, mode, agent, Arrays.asList(mutators));
    } finally {
      agent.close();
    }
  }

  private void createEngineAndRun(ReportOptions data,
      ExecutionMode mode,
      JavaAgent agent,
      Collection<String> mutators) {

    final CoverageOptions coverageOptions = createCoverageOptions(data);

    final LaunchOptions launchOptions = new LaunchOptions(agent,
        new DefaultJavaExecutableLocator(), data.getJvmArgs(),
        new HashMap<>());

    final PathFilter pf = new PathFilter(
        Prelude.not(new DefaultDependencyPathPredicate()),
        Prelude.not(new DefaultDependencyPathPredicate()));
    final ProjectClassPaths cps = new ProjectClassPaths(data.getClassPath(),
        data.createClassesFilter(), pf);

    final Timings timings = new Timings(new NoTestStats());
    final CodeSource code = new DefaultCodeSource(cps);

    final CoverageGenerator coverageGenerator = new DefaultCoverageGenerator(
            null, coverageOptions, launchOptions, code, new NullCoverageExporter(),
            new NoTestStats(), timings, Verbosity.DEFAULT);

    final CoverageDatabase coverageData = coverageGenerator.calculateCoverage(c -> true);

    final Collection<ClassName> codeClasses = code.getCodeUnderTestNames();

    final EngineArguments arguments = EngineArguments.arguments()
        .withMutators(mutators);

    final MutationEngine engine = new GregorEngineFactory().createEngine(arguments);

    final MutationConfig mutationConfig = new MutationConfig(engine,
        launchOptions);

    final ClassloaderByteArraySource bas = new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader());

    final MutationInterceptor emptyIntercpetor = CompoundMutationInterceptor.nullInterceptor();

    final MutationSource source = new MutationSource(mutationConfig, new DefaultTestPrioritiser(
            coverageData), bas, emptyIntercpetor);


    final WorkerFactory wf = new WorkerFactory(null,
        coverageOptions.getPitConfig(), mutationConfig, arguments,
        new PercentAndConstantTimeoutStrategy(data.getTimeoutFactor(),
            data.getTimeoutConstant()), data.getVerbosity(), false, data.getClassPath()
            .getLocalClassPath(), data.getFeatures());




    final MutationTestBuilder builder = new MutationTestBuilder(mode, wf,
        new NullHistory(), source, new DefaultGrouper(0));

    final List<MutationAnalysisUnit> tus = builder
        .createMutationTestUnits(codeClasses);

    this.mae.run(tus);
  }

  private CoverageOptions createCoverageOptions(ReportOptions data) {
    return new CoverageOptions(data.getTargetClasses(),data.getExcludedClasses(), this.config,
        data.getVerbosity(), data.getFeatures());
  }

  protected void verifyResults(final DetectionStatus... detectionStatus) {
    final List<DetectionStatus> expected = Arrays.asList(detectionStatus);
    final List<DetectionStatus> actual = this.metaDataExtractor
        .getDetectionStatus();

    Collections.sort(expected);
    Collections.sort(actual);

    assertEquals(expected, actual);
  }

  protected void verifyLineNumbers(final Integer... lineNumbers) {
    final List<Integer> expected = Arrays.asList(lineNumbers);
    final List<Integer> actual = this.metaDataExtractor.getLineNumbers();

    Collections.sort(expected);
    Collections.sort(actual);

    assertEquals(expected, actual);
  }

}
