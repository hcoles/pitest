package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.pitest.mutationtest.LocationMother.aLocation;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

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

public class IncrementalAnalyserTest {

  private IncrementalAnalyser testee;

  @Mock
  private CodeHistory         history;

  @Mock
  private CoverageDatabase    coverage;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new IncrementalAnalyser(this.history, this.coverage);
  }

  @Test
  public void shouldStartNewMutationsAtAStatusOfNotStarted() {
    final MutationDetails md = makeMutation("foo");
    when(this.history.getPreviousResult(any(MutationIdentifier.class)))
    .thenReturn(Option.<MutationStatusTestPair> none());

    final Collection<MutationResult> actual = this.testee.analyse(Collections
        .singletonList(md));

    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next()
        .getStatus());
  }

  @Test
  public void shouldStartPreviousSurvivedMutationsAtAStatusOfNotStartedWhenCoverageHasChanged() {
    final MutationDetails md = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.SURVIVED);
    when(
        this.history.hasCoverageChanged(any(ClassName.class),
            any(BigInteger.class))).thenReturn(true);
    final Collection<MutationResult> actual = this.testee.analyse(Collections
        .singletonList(md));
    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next()
        .getStatus());
  }

  @Test
  public void shouldStartPreviousSurvivedMutationsAtAStatusOfSurvivedWhenCoverageHasNotChanged() {
    final MutationDetails md = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.SURVIVED);
    when(
        this.history.hasCoverageChanged(any(ClassName.class),
            any(BigInteger.class))).thenReturn(false);
    final Collection<MutationResult> actual = this.testee.analyse(Collections
        .singletonList(md));
    assertEquals(DetectionStatus.SURVIVED, actual.iterator().next().getStatus());
  }

  @Test
  public void shouldStartPreviousTimedOutMutationsAtAStatusOfNotStartedWhenClassHasChanged() {
    final MutationDetails md = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.TIMED_OUT);
    when(this.history.hasClassChanged(any(ClassName.class))).thenReturn(true);
    final Collection<MutationResult> actual = this.testee.analyse(Collections
        .singletonList(md));

    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next()
        .getStatus());
  }

  @Test
  public void shouldStartPreviousTimedOutMutationsAtAStatusOfTimedOutWhenClassHasNotChanged() {
    final MutationDetails md = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.TIMED_OUT);
    when(this.history.hasClassChanged(any(ClassName.class))).thenReturn(false);
    final Collection<MutationResult> actual = this.testee.analyse(Collections
        .singletonList(md));

    assertEquals(DetectionStatus.TIMED_OUT, actual.iterator().next()
        .getStatus());
  }

  @Test
  public void shouldStartPreviousKilledMutationsAtAStatusOfNotStartedWhenNeitherClassOrTestHasChanged() {
    final MutationDetails md = makeMutation("foo");
    final String killingTest = "fooTest";
    setHistoryForAllMutationsTo(DetectionStatus.KILLED, killingTest);

    final Collection<TestInfo> tests = Collections.singleton(new TestInfo(
        "TEST_CLASS", killingTest, 0, Option.<ClassName> none(), 0));
    when(this.coverage.getTestsForClass(any(ClassName.class)))
    .thenReturn(tests);
    when(this.history.hasClassChanged(any(ClassName.class))).thenReturn(false);
    final MutationResult actual = this.testee
        .analyse(Collections.singletonList(md)).iterator().next();

    assertEquals(DetectionStatus.KILLED, actual.getStatus());
    assertEquals(Option.some(killingTest), actual.getKillingTest());
  }

  @Test
  public void shouldStartPreviousKilledMutationsAtAStatusOfKilledWhenNeitherClassOrTestHasChanged() {
    final MutationDetails md = makeMutation("foo");
    final String killingTest = "fooTest";
    setHistoryForAllMutationsTo(DetectionStatus.KILLED, killingTest);

    final Collection<TestInfo> tests = Collections.singleton(new TestInfo(
        "TEST_CLASS", killingTest, 0, Option.<ClassName> none(), 0));
    when(this.coverage.getTestsForClass(any(ClassName.class)))
    .thenReturn(tests);
    when(this.history.hasClassChanged(ClassName.fromString("foo"))).thenReturn(
        false);
    when(this.history.hasClassChanged(ClassName.fromString("TEST_CLASS")))
    .thenReturn(true);
    final MutationResult actual = this.testee
        .analyse(Collections.singletonList(md)).iterator().next();

    assertEquals(DetectionStatus.NOT_STARTED, actual.getStatus());
  }

  private MutationDetails makeMutation(final String method) {
    final MutationIdentifier id = aMutationId().withLocation(
        aLocation().withMethod(method)).build();
    return new MutationDetails(id, "file", "desc", 1, 2);
  }

  private void setHistoryForAllMutationsTo(final DetectionStatus status) {
    setHistoryForAllMutationsTo(status, "bar");
  }

  private void setHistoryForAllMutationsTo(final DetectionStatus status,
      final String test) {
    when(this.history.getPreviousResult(any(MutationIdentifier.class)))
    .thenReturn(Option.some(new MutationStatusTestPair(0, status, test)));
  }

}
