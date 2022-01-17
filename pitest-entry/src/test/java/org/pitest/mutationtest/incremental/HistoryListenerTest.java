package org.pitest.mutationtest.incremental;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;

import static org.mockito.Mockito.verify;

public class HistoryListenerTest {

  private HistoryListener testee;

  @Mock
  private HistoryStore    store;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.testee = new HistoryListener(this.store);
  }

  @Test
  public void shouldRecordMutationResults() {
    final MutationResult mr = makeResult();
    final ClassMutationResults metaData = MutationTestResultMother
        .createClassResults(mr);
    this.testee.handleMutationResult(metaData);
    verify(this.store).recordResult(mr);
  }

  private MutationResult makeResult() {
    return new MutationResult(
        MutationTestResultMother.createDetails(), MutationStatusTestPair.notAnalysed(0,
            DetectionStatus.KILLED));
  }

}
