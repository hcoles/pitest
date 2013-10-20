package org.pitest.mutationtest.build;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.build.KnownStatusMutationTestUnit;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.testapi.ResultCollector;

public class KnownStatusMutationTestUnitTest {

  private KnownStatusMutationTestUnit testee;

  @Mock
  private ResultCollector             rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

  }

  @Test
  public void shouldCallNotifyStart() {
    this.testee = new KnownStatusMutationTestUnit(
        Collections.<MutationResult> emptyList());
    this.testee.execute(null, this.rc);
    verify(this.rc).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldCreateMutationMetaDataForSuppliedResults() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(1,
            DetectionStatus.KILLED, "foo"));
    final List<MutationResult> mutations = Arrays.asList(mr);
    this.testee = new KnownStatusMutationTestUnit(mutations);
    this.testee.execute(null, this.rc);

    final MutationMetaData expected = new MutationMetaData(mutations);
    verify(this.rc).notifyEnd(this.testee.getDescription(), expected);
  }

  @Test
  public void shouldHaveHighPriorityToAnalyse() {
    this.testee = new KnownStatusMutationTestUnit(
        Collections.<MutationResult> emptyList());
    assertEquals(Integer.MAX_VALUE, this.testee.priority());
  }

}
