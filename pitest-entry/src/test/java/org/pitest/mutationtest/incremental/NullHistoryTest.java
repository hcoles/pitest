package org.pitest.mutationtest.incremental;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetailsMother;

import static org.assertj.core.api.Assertions.assertThat;

public class NullHistoryTest {

  private final NullHistory testee = new NullHistory();

  @Test
  public void returnsNoAnalysedMutants() {
    assertThat(testee.analyse(MutationDetailsMother.aMutationDetail().build(2))).isEmpty();
  }

}
