package org.pitest.mutationtest.engine.gregor.inlinedcode;

import java.util.Collection;

import org.pitest.mutationtest.MutationDetails;

public class NoInlinedCodeDetection implements InlinedCodeFilter {

  public Collection<MutationDetails> process(
      final Collection<MutationDetails> mutations) {
    return mutations;
  }

}
