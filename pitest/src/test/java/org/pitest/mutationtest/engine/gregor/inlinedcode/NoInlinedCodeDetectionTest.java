package org.pitest.mutationtest.engine.gregor.inlinedcode;

import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationDetailsMother;

public class NoInlinedCodeDetectionTest {

  @Test
  public void shouldReturnSuppliedMutationsUnchanged() {
    final NoInlinedCodeDetection testee = new NoInlinedCodeDetection();
    final Collection<MutationDetails> mutations = Arrays
        .asList(MutationDetailsMother.makeMutation());
    assertSame(mutations, testee.process(mutations));
  }

}
