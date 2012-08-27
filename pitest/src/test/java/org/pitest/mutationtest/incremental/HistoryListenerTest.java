package org.pitest.mutationtest.incremental;

import static org.mockito.Mockito.*;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class HistoryListenerTest {
  
  private HistoryListener testee;
  
  @Mock
  private HistoryStore store;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new HistoryListener(store);
  }
  
  @Test
  public void shouldRecordMutationResults() {
    final MutationResult mr = makeResult();
    MutationMetaData metaData = MutationTestResultMother
        .createMetaData(mr);
    testee.handleMutationResult(metaData);
    verify(store).recordResult(mr);
  }
  
  private MutationResult makeResult() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(0,
            DetectionStatus.KILLED));
    return mr;
  }

}
