package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.mutationtest.engine.MutationDetails;

/**
 * Chooses a start status for a mutation
 */
public interface MutationAnalyser {

  Collection<MutationResult> analyse(
      Collection<MutationDetails> mutationsForClasses);

}
