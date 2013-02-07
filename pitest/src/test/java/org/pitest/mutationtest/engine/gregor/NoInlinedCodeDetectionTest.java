package org.pitest.mutationtest.engine.gregor;

import static org.junit.Assert.assertSame;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.inlinedcode.NoInlinedCodeDetection;

public class NoInlinedCodeDetectionTest {

  @Test
  public void shouldReturnSuppliedMutationsUnchanged() {
    final NoInlinedCodeDetection testee = new NoInlinedCodeDetection();
    final Collection<MutationDetails> mutations = Arrays.asList(makeMutation());
    assertSame(mutations, testee.process(mutations));
  }

  private MutationDetails makeMutation() {
    return new MutationDetails(new MutationIdentifier("foo", 1, "foo"), null,
        null, null, 0, 0);
  }

}
