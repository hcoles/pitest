package org.pitest.mutationtest;

import java.util.Collection;


/**
 * Chooses a start status for a mutation
 */
public interface MutationAnalyser {

  Collection<MutationResult> analyse(
      Collection<MutationDetails> mutationsForClasses);

}
