package org.pitest.mutationtest.incremental;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.coverage.TestInfo;
import org.pitest.functional.Option;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

public class IncrementalAnalyserTest {

  private IncrementalAnalyser testee;

  @Mock
  private CodeHistory         history;

  @Mock
  private CoverageDatabase    coverage;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new IncrementalAnalyser(history, coverage);
  }

  @Test
  public void shouldStartNewMutationsAtAStatusOfNotStarted() {
    MutationDetails mutationDetails = makeMutation("foo");
    when(history.getPreviousResult(any(MutationIdentifier.class))).thenReturn(Option.<MutationStatusTestPair>none());

    Collection<MutationResult> actual = testee.analyse(Collections.singletonList(mutationDetails));

    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next().getStatus());
  }

  @Test
  public void shouldStartPreviousSurvivedMutationsAtAStatusOfNotStartedWhenCoverageHasChanged() {
    MutationDetails mutationDetails = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.SURVIVED);
    when(history.hasCoverageChanged(any(ClassName.class),any(BigInteger.class))).thenReturn(true);

    Collection<MutationResult> actual = testee.analyse(Collections.singletonList(mutationDetails));

    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next().getStatus());
  }

  @Test
  public void shouldStartPreviousSurvivedMutationsAtAStatusOfSurvivedWhenCoverageHasNotChanged() {
    MutationDetails mutationDetails = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.SURVIVED);
    when(history.hasCoverageChanged(any(ClassName.class),any(BigInteger.class))).thenReturn(false);

    Collection<MutationResult> actual = testee.analyse(Collections.singletonList(mutationDetails));

    assertEquals(DetectionStatus.SURVIVED, actual.iterator().next().getStatus());
  }

  @Test
  public void shouldStartPreviousTimedOutMutationsAtAStatusOfNotStartedWhenClassHasChanged() {
    MutationDetails mutationDetails = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.TIMED_OUT);
    when(history.hasClassChanged(any(ClassName.class))).thenReturn(true);

    Collection<MutationResult> actual = testee.analyse(Collections.singletonList(mutationDetails));

    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next().getStatus());
  }

  @Test
  public void shouldStartPreviousTimedOutMutationsAtAStatusOfTimedOutWhenClassHasNotChanged() {
    MutationDetails mutationDetails = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.TIMED_OUT);
    when(history.hasClassChanged(any(ClassName.class))).thenReturn(false);

    Collection<MutationResult> actual = testee.analyse(Collections.singletonList(mutationDetails));

    assertEquals(DetectionStatus.TIMED_OUT, actual.iterator().next().getStatus());
  }

  @Test
  public void shouldStartPreviousKilledMutationsAtAStatusOfNotStartedWhenNeitherClassOrTestHasChanged() {
    MutationDetails md = makeMutation("foo");
    String killingTest = "fooTest";
    setHistoryForAllMutationsTo(DetectionStatus.KILLED, killingTest);

    Collection<TestInfo> tests = Collections.singleton(new TestInfo(
        "TEST_CLASS", killingTest, 0, Option.<ClassName> none(), 0));
    when(coverage.getTestsForClass(any(ClassName.class)))
        .thenReturn(tests);
    when(history.hasClassChanged(any(ClassName.class))).thenReturn(false);
    MutationResult actual = testee
        .analyse(Collections.singletonList(md)).iterator().next();

    assertEquals(DetectionStatus.KILLED, actual.getStatus());
    assertEquals(Option.some(killingTest), actual.getKillingTest());
  }

  @Test
  public void shouldStartPreviousKilledMutationsAtAStatusOfKilledWhenNeitherClassOrTestHasChanged() {
    MutationDetails mutationDetails = makeMutation("foo");
    String killingTest = "fooTest";
    setHistoryForAllMutationsTo(DetectionStatus.KILLED, killingTest);

    Collection<TestInfo> tests = Collections.singleton(new TestInfo(        "TEST_CLASS", killingTest, 0, Option.<ClassName> none(), 0));
    when(coverage.getTestsForClass(any(ClassName.class))).thenReturn(tests);
    when(history.hasClassChanged(ClassName.fromString("foo"))).thenReturn(false);
    when(history.hasClassChanged(ClassName.fromString("TEST_CLASS"))).thenReturn(true);

    MutationResult actual = testee.analyse(Collections.singletonList(mutationDetails)).iterator().next();

    assertEquals(DetectionStatus.NOT_STARTED, actual.getStatus());
  }

  private MutationDetails makeMutation(String method) {
    MutationIdentifier id =  aMutationId().withLocation(aLocation().withMethod(method)).build();
    return new MutationDetails(id, "file", "desc", 1, 2);
  }

  private void setHistoryForAllMutationsTo(DetectionStatus status) {
    setHistoryForAllMutationsTo(status, "bar");
  }

  private void setHistoryForAllMutationsTo(DetectionStatus status, String test) {
      Option<MutationStatusTestPair> testPairOption = Option.some(new MutationStatusTestPair(0, status, test));
      when(history.getPreviousResult(any(MutationIdentifier.class))).thenReturn(testPairOption);
  }
}
