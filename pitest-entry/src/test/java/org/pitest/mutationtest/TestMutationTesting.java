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

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static org.pitest.mutationtest.DetectionStatus.MEMORY_ERROR;
import static org.pitest.mutationtest.DetectionStatus.NON_VIABLE;
import static org.pitest.mutationtest.DetectionStatus.NO_COVERAGE;
import static org.pitest.mutationtest.DetectionStatus.SURVIVED;
import static org.pitest.mutationtest.DetectionStatus.TIMED_OUT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.mockito.MockitoAnnotations;
import org.pitest.SystemTest;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.CoverageGenerator;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.DefaultCoverageGenerator;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.FCollection;
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
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.TestPluginArguments;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.execute.MutationAnalysisExecutor;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaAgent;
import org.pitest.process.LaunchOptions;
import org.pitest.simpletest.SimpleTestPlugin;
import org.pitest.simpletest.TestAnnotationForTesting;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Timings;

import com.example.MutationsInNestedClasses;
import com.example.MutationsInNestedClassesTest;

@Category(SystemTest.class)
public class TestMutationTesting {

  private MutationAnalysisExecutor mae;
  private TestPluginArguments      config;

  private MetaDataExtractor        metaDataExtractor;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.config = TestPluginArguments.defaults().withTestPlugin(SimpleTestPlugin.NAME);
    this.metaDataExtractor = new MetaDataExtractor();
    this.mae = new MutationAnalysisExecutor(1,
        Collections
            .<MutationResultListener> singletonList(this.metaDataExtractor));
  }

  public static class NoMutations {

  }

  public static class OneMutationOnly {
    public static int returnOne() {
      return 1;
    }
  }

  public static class ThreeMutations {
    public static int returnOne() {
      return 1;
    }

    public static int returnTwo() {
      return 2;
    }

    public static int returnThree() {
      return 3;
    }
  }

  public static class OneMutationFullTest {
    @TestAnnotationForTesting
    public void testReturnOne() {
      assertEquals(1, OneMutationOnly.returnOne());
    }
  }

  @Test
  public void shouldKillAllCoveredMutations() {
    run(OneMutationOnly.class, OneMutationFullTest.class,
        "RETURN_VALS");
    verifyResults(KILLED);
  }

  public static class ThreeMutationsTwoMeaningfullTests {
    @TestAnnotationForTesting
    public void testReturnOne() {
      assertEquals(1, ThreeMutations.returnOne());
    }

    @TestAnnotationForTesting
    public void testReturnTwo() {
      assertEquals(2, ThreeMutations.returnTwo());
    }

    @TestAnnotationForTesting
    public void coverButDoNotTestReturnThree() {
      ThreeMutations.returnThree();
    }
  }

  @Test
  public void shouldDetectedMixOfSurvivingAndKilledMutations() {
    run(ThreeMutations.class, ThreeMutationsTwoMeaningfullTests.class,
        "RETURN_VALS");
    verifyResults(SURVIVED, KILLED, KILLED);
  }

  public static class FailingTest {
    @TestAnnotationForTesting
    public void fail() {
      assertEquals(1, 2);
    }
  }

  public static class NoMutationsTest {
    @TestAnnotationForTesting
    public void pass() {

    }
  }

  @Test
  public void shouldReportNoResultsIfNoMutationsPossible() {
    run(NoMutations.class, NoMutationsTest.class, "RETURN_VALS");
    verifyResults();
  }

  public static class NoTests {

  }

  @Test
  public void shouldReportStatusOfNoCoverageWhenNoTestsAvailable() {
    run(ThreeMutations.class, NoTests.class, "RETURN_VALS");
    verifyResults(NO_COVERAGE, NO_COVERAGE, NO_COVERAGE);
  }

  public static class OneMutationTest {

  }

  public static class InfiniteLoop {
    public static int loop() {
      int i = 1;
      do {
        i++;
        try {
          Thread.sleep(1);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      } while (i < 1);
      i++;
      return i;
    }
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

  public static class OneMutationFullTestWithSystemPropertyDependency {
    @TestAnnotationForTesting
    public void testReturnOne() {
      if (System.getProperty("foo").equals("foo")) {
        assertEquals(1, OneMutationOnly.returnOne());
      }
    }
  }

  @Test
  public void shouldExportSystemPropertiesToMinionProcess() {
    // System.setProperty("foo", "foo");
    // note surefire is configured to launch this test with -Dfoo=foo
    run(OneMutationOnly.class,
        OneMutationFullTestWithSystemPropertyDependency.class,
        "RETURN_VALS");
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

  public static class EatsMemoryWhenMutated {
    public static int loop() throws InterruptedException {
      int i = 1;
      final List<String[]> vals = new ArrayList<>();
      Thread.sleep(1500);
      do {
        i++;
        vals.add(new String[9999999]);
        vals.add(new String[9999999]);
        vals.add(new String[9999999]);
        vals.add(new String[9999999]);
      } while (i < 1);
      i++;
      return i;
    }
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
        "RETURN_VALS");
    verifyResults(SURVIVED, SURVIVED);
  }

  @Test
  @Ignore("too brittle")
  public void shouldRecordCorrectLineNumberForMutations() {
    run(OneMutationOnly.class, OneMutationFullTest.class,
        "RETURN_VALS");
    verifyLineNumbers(111);
  }

  private void run(final Class<?> clazz, final Class<?> test,
      final String ... mutators) {

    final ReportOptions data = new ReportOptions();

    final Set<Predicate<String>> tests = Collections.singleton(Prelude
        .isEqualTo(test.getName()));
    data.setTargetTests(tests);
    data.setDependencyAnalysisMaxDistance(-1);

    final Set<String> mutees = Collections.singleton(clazz.getName() + "*");
    data.setTargetClasses(mutees);

    data.setTimeoutConstant(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT);
    data.setTimeoutFactor(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR);

    final JavaAgent agent = new JarCreatingJarFinder();

    try {
      createEngineAndRun(data, agent, Arrays.asList(mutators));
    } finally {
      agent.close();
    }
  }

  private void createEngineAndRun(final ReportOptions data,
      final JavaAgent agent,
      final Collection<String> mutators) {

    // data.setConfiguration(this.config);
    final CoverageOptions coverageOptions = createCoverageOptions(data);

    final LaunchOptions launchOptions = new LaunchOptions(agent,
        new DefaultJavaExecutableLocator(), data.getJvmArgs(),
        new HashMap<String, String>());

    final PathFilter pf = new PathFilter(
        Prelude.not(new DefaultDependencyPathPredicate()),
        Prelude.not(new DefaultDependencyPathPredicate()));
    final ProjectClassPaths cps = new ProjectClassPaths(data.getClassPath(),
        data.createClassesFilter(), pf);

    final Timings timings = new Timings();
    final CodeSource code = new CodeSource(cps);

    final CoverageGenerator coverageGenerator = new DefaultCoverageGenerator(
        null, coverageOptions, launchOptions, code, new NullCoverageExporter(),
        timings, false);

    final CoverageDatabase coverageData = coverageGenerator.calculateCoverage();

    final Collection<ClassName> codeClasses = FCollection.map(code.getCode(),
        ClassInfo.toClassName());

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
            data.getTimeoutConstant()), data.isVerbose(), false, data.getClassPath()
            .getLocalClassPath());




    final MutationTestBuilder builder = new MutationTestBuilder(wf,
        new NullAnalyser(), source, new DefaultGrouper(0));

    final List<MutationAnalysisUnit> tus = builder
        .createMutationTestUnits(codeClasses);

    this.mae.run(tus);
  }

  private CoverageOptions createCoverageOptions(ReportOptions data) {
    return new CoverageOptions(data.getTargetClasses(),data.getExcludedClasses(), this.config,
        data.isVerbose(), data.getDependencyAnalysisMaxDistance());
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
