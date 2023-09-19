package org.pitest.mutationtest.incremental;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;

import static org.mockito.Mockito.verify;

public class HistoryListenerTest {

  private HistoryListener testee;

  @Mock
  private History store;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    this.testee = new HistoryListener(this.store);
  }

  @Test
  public void closesTheAttachedHistory() {
    this.testee.runEnd();
    verify(this.store).close();
  }


}
