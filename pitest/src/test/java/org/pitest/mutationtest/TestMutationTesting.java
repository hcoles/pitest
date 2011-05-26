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
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.KILLED;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.MEMORY_ERROR;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.NON_VIABLE;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.SURVIVED;
import static org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus.TIMED_OUT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.DefaultStaticConfig;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.TestResult;
import org.pitest.annotations.ClassUnderTest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.config.DefaultMutationEngineConfiguration;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.mutationtest.instrument.ResultsReader.MutationResult;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;
import org.pitest.testutil.ConfigurationForTesting;
import org.pitest.testutil.IgnoreAnnotationForTesting;
import org.pitest.testutil.TestAnnotationForTesting;

public class TestMutationTesting {

  private Pitest              pit;
  private Container           container;
  @Mock
  private TestListener        listener;
  private DefaultStaticConfig staticConfig;

  private MetaDataExtractor   metaDataExtractor;

  public static class MetaDataExtractor implements TestListener {

    private final List<MutationResult> data = new ArrayList<MutationResult>();

    public List<MutationResult> getData() {
      return this.data;
    }

    public List<DetectionStatus> getDetectionStatus() {
      final List<DetectionStatus> dss = new ArrayList<DetectionStatus>();
      for (final MutationResult each : this.data) {
        dss.add(each.status);
      }
      return dss;
    }

    private void accumulateMetaData(final TestResult tr) {
      final Option<MutationMetaData> d = tr.getValue(MutationMetaData.class);
      if (d.hasSome()) {
        this.data.addAll(d.value().getMutations());
      }
    }

    public void onTestError(final TestResult tr) {
      accumulateMetaData(tr);
    }

    public void onTestFailure(final TestResult tr) {
      accumulateMetaData(tr);
    }

    public void onTestSkipped(final TestResult tr) {
      accumulateMetaData(tr);
    }

    public void onTestStart(final Description d) {

    }

    public void onTestSuccess(final TestResult tr) {
      accumulateMetaData(tr);
    }

    public void onRunEnd() {
      // TODO Auto-generated method stub

    }

    public void onRunStart() {
      // TODO Auto-generated method stub

    }

  }

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.metaDataExtractor = new MetaDataExtractor();
    this.container = new UnContainer();
    this.staticConfig = new DefaultStaticConfig();
    this.staticConfig.addTestListener(this.listener);
    this.staticConfig.addTestListener(this.metaDataExtractor);
    this.pit = new Pitest(this.staticConfig, new ConfigurationForTesting());
  }

  public static class NoMutations {

  }

  public static class OneMutation {
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

  @ClassUnderTest(OneMutation.class)
  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class OneMutationFullTest {
    @TestAnnotationForTesting
    public void testReturnOne() {
      assertEquals(1, OneMutation.returnOne());
    }
  }

  @Test
  public void shouldPassIfAllMutationsKilled() {
    run(OneMutationFullTest.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    verifyResults(KILLED);
  }

  @ClassUnderTest(ThreeMutations.class)
  @MutationTest(threshold = 67, mutators = Mutator.RETURN_VALS)
  public static class ThreeMutationsTwoTests {
    @TestAnnotationForTesting
    public void testReturnOne() {
      assertEquals(1, ThreeMutations.returnOne());
    }

    @TestAnnotationForTesting
    public void testReturnTwo() {
      assertEquals(2, ThreeMutations.returnTwo());
    }

  }

  @Test
  public void shouldFailIfDetectsLessThanThresholdPercentOfMutations() {
    run(ThreeMutationsTwoTests.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
    verifyResults(SURVIVED, KILLED, KILLED);
  }

  @ClassUnderTest(ThreeMutations.class)
  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class FailingTest {
    @TestAnnotationForTesting
    public void fail() {
      assertEquals(1, 2);
    }
  }

  @Test
  public void shouldReportErrorIfTestsFailsWithoutMutation() {
    run(FailingTest.class);
    // one failure for the test, one error for the mutation test
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
    verify(this.listener, times(1)).onTestError((any(TestResult.class)));
    verifyResults();
  }

  @ClassUnderTest(NoMutations.class)
  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class NoMutationsTest {
    @TestAnnotationForTesting
    public void pass() {

    }
  }

  @Test
  public void shouldSkipsTestIfNoMutationsPossible() {
    run(NoMutationsTest.class);
    verify(this.listener, times(1)).onTestSuccess((any(TestResult.class)));
    verify(this.listener, times(1)).onTestSkipped((any(TestResult.class)));
    verifyResults();
  }

  @ClassUnderTest(ThreeMutations.class)
  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class NoTests {
    @TestAnnotationForTesting
    @IgnoreAnnotationForTesting
    public void fail() {
      System.out.println("oops");
      assertEquals(1, 2);
    }
  }

  @Test
  public void shouldReportFailureIfNoTestsAvailable() {
    run(NoTests.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
    verifyResults();
  }

  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class OneMutationTest {

  }

  @Test
  public void shouldGuessesCorrectTesteeNameWhenTestNameEndsWithTest() {
    run(OneMutationTest.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public class TestOneMutation {

  }

  @Test
  public void shouldGuesseCorrectTesteeNameWhenTestNameStartsWithTestForInnerClass() {
    run(TestOneMutation.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
  }

  @MutationTest(threshold = 100, mutators = Mutator.INCREMENTS)
  public class TestMutableIncrement {

  }

  @Test
  public void shouldGuessCorrectTesteeNameWhenTestNameStartsWithTest() {
    run(TestMutableIncrement.class);
    verify(this.listener, times(1)).onTestFailure((any(TestResult.class)));
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

  @ClassUnderTest(InfiniteLoop.class)
  @MutationTest(threshold = 100, mutators = Mutator.INCREMENTS)
  public static class InfiniteLoopTest {
    @TestAnnotationForTesting()
    public void pass() {
      assertEquals(3, InfiniteLoop.loop());
    }
  }

  @Test(timeout = 30000)
  public void shouldDetectAndEscapeFromInfiniteLoopsCausedByMutations() {
    run(InfiniteLoopTest.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    verifyResults(KILLED, TIMED_OUT);
  }

  @ClassUnderTest(OneMutation.class)
  @MutationTest(threshold = 100, mutators = Mutator.RETURN_VALS)
  public static class OneMutationFullTestWithSystemPropertyDependency {
    @TestAnnotationForTesting
    public void testReturnOne() {
      if (System.getProperty("foo").equals("foo")) {
        assertEquals(1, OneMutation.returnOne());
      }
    }
  }

  @Test
  public void shouldExportSystemPropertiesToSlaveProcess() {
    System.setProperty("foo", "foo");
    run(OneMutationFullTestWithSystemPropertyDependency.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    verifyResults(KILLED);
  }

  public static class UnviableClassConfigurationFactory implements
      MutationConfigFactory {

    public MutationConfig createConfig(final MutationTest config) {
      final DefaultMutationEngineConfiguration engineConfig = new DefaultMutationEngineConfiguration(
          True.<MethodInfo> all(), Collections.<String> emptyList(),
          Collections
              .<MethodMutatorFactory> singleton(new UnviableClassMutator()));
      final GregorMutationEngine engine = new GregorMutationEngine(engineConfig);
      return new MutationConfig(engine, MutationTestType.TEST_CENTRIC, 0,
          Collections.<String> emptyList());
    }

  }

  @ClassUnderTest(OneMutation.class)
  @MutationTest(threshold = 100, mutationConfigFactory = UnviableClassConfigurationFactory.class)
  public static class UnviableMutationsTest {
    @TestAnnotationForTesting
    public void test() {
      OneMutation.returnOne();
    }
  }

  @Test
  public void shouldDetectUnviableMutations() {
    run(UnviableMutationsTest.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    verifyResultsContain(NON_VIABLE);
  }

  public static class EatsMemoryWhenMutated {
    public static int loop() throws InterruptedException {
      int i = 1;
      final List<String[]> vals = new ArrayList<String[]>();
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

  @ClassUnderTest(EatsMemoryWhenMutated.class)
  @MutationTest(threshold = 100, mutators = Mutator.INCREMENTS)
  public static class EatsMemoryTest {
    @TestAnnotationForTesting()
    public void pass() throws InterruptedException {
      assertEquals(3, EatsMemoryWhenMutated.loop());
    }
  }

  @Test(timeout = 30000)
  @Ignore
  public void shouldRecoverFromOutOfMemoryError() {
    run(EatsMemoryTest.class);
    verifyResults(KILLED, MEMORY_ERROR);
  }

  private void run(final Class<?> clazz) {
    this.pit.run(this.container, clazz);
  }

  protected void verifyResults(final DetectionStatus... detectionStatus) {
    final List<DetectionStatus> expected = Arrays.asList(detectionStatus);
    final List<DetectionStatus> actual = this.metaDataExtractor
        .getDetectionStatus();

    Collections.sort(expected);
    Collections.sort(actual);

    assertEquals(expected, actual);
  }

  private void verifyResultsContain(final DetectionStatus status) {
    assertTrue(this.metaDataExtractor.getDetectionStatus().contains(status));

  }

}
