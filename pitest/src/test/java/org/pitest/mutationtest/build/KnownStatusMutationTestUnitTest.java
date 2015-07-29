package org.pitest.mutationtest.build;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.report.MutationTestResultMother;

public class KnownStatusMutationTestUnitTest {

  private KnownStatusMutationTestUnit testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);

  }

  @Test
  public void shouldCreateMutationMetaDataForSuppliedResults() throws Exception {
    final MutationResult mr = new MutationResult(
        MutationTestResultMother.createDetails(), new MutationStatusTestPair(1,
            DetectionStatus.KILLED, "foo"));
    final List<MutationResult> mutations = Arrays.asList(mr);
    this.testee = new KnownStatusMutationTestUnit(mutations);
    MutationMetaData actual = this.testee.call();

    final MutationMetaData expected = new MutationMetaData(mutations);
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void shouldHaveHighPriorityToAnalyse() {
    this.testee = new KnownStatusMutationTestUnit(
        Collections.<MutationResult> emptyList());
    assertEquals(Integer.MAX_VALUE, this.testee.priority());
  }

}
