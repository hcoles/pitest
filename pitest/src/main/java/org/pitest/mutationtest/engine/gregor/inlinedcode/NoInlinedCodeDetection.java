package org.pitest.mutationtest.engine.gregor.inlinedcode;

import java.util.Collection;

import org.pitest.mutationtest.engine.MutationDetails;

public class NoInlinedCodeDetection implements InlinedCodeFilter {

  @Override
  public Collection<MutationDetails> process(
      final Collection<MutationDetails> mutations) {
    return mutations;
  }

}
