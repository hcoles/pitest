package org.pitest.mutationtest.engine.gregor.inlinedcode;

import java.util.Collection;

import org.pitest.mutationtest.engine.MutationDetails;

public interface InlinedCodeFilter {

  Collection<MutationDetails> process(Collection<MutationDetails> mutations);

}
