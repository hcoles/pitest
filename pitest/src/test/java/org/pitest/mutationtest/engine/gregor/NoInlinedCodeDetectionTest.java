package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.MutationDetailsMother;
import org.pitest.mutationtest.engine.gregor.inlinedcode.NoInlinedCodeDetection;

public class NoInlinedCodeDetectionTest {

  @Test
  public void shouldReturnSuppliedMutationsUnchanged() {
    final NoInlinedCodeDetection testee = new NoInlinedCodeDetection();
    final Collection<MutationDetails> mutations = Arrays
        .asList(MutationDetailsMother.makeMutation());
    assertSame(mutations, testee.process(mutations));
  }

}
