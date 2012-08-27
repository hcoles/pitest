package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Collection;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.Option;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class IncrementalAnalyserTest {

  private IncrementalAnalyser testee;

  @Mock
  private CodeHistory         history;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new IncrementalAnalyser(this.history);
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
  public void shouldStartPreviousKilledMutationsAtAStatusOfNotStarted() {
    final MutationDetails md = makeMutation("foo");
    setHistoryForAllMutationsTo(DetectionStatus.KILLED);
    final Collection<MutationResult> actual = this.testee.analyse(Collections
        .singletonList(md));

    assertEquals(DetectionStatus.NOT_STARTED, actual.iterator().next()
        .getStatus());
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

  private MutationDetails makeMutation(final String method) {
    final MutationIdentifier id = new MutationIdentifier("foo", 0, "mutator");
    return new MutationDetails(id, "file", "desc", method, 1, 2);
  }

  private void setHistoryForAllMutationsTo(final DetectionStatus status) {
    when(this.history.getPreviousResult(any(MutationIdentifier.class)))
        .thenReturn(
            Option.some(new MutationStatusTestPair(0, status, "footest")));
  }

}
