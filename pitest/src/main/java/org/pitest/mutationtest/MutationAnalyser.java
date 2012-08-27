package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.mutationtest.results.MutationResult;

/**
 * Chooses a start status for a mutation
 */
public interface MutationAnalyser {

  Collection<MutationResult> analyse(
      Collection<MutationDetails> mutationsForClasses);

}
