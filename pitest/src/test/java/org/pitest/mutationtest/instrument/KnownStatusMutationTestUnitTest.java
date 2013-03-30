package org.pitest.mutationtest.instrument;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.ResultCollector;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

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
        Collections.<String> emptyList(),
        Collections.<MutationResult> emptyList());
    this.testee.execute(null, this.rc);
    verify(this.rc).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldCreateMutationMetaDataForSuppliedResults() {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(1,
            DetectionStatus.KILLED, "foo"));
    final List<String> mutators = Arrays.asList("foo", "bar");
    final List<MutationResult> mutations = Arrays.asList(mr);
    this.testee = new KnownStatusMutationTestUnit(mutators, mutations);
    this.testee.execute(null, this.rc);

    final MutationMetaData expected = new MutationMetaData(mutators, mutations);
    verify(this.rc).notifyEnd(this.testee.getDescription(), expected);
  }

  @Test
  public void shouldHaveHighPriorityToAnalyse() {
    this.testee = new KnownStatusMutationTestUnit(
        Collections.<String> emptyList(),
        Collections.<MutationResult> emptyList());
    assertEquals(Integer.MAX_VALUE, this.testee.priority());
  }

}
