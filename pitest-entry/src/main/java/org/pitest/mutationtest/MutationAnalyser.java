package org.pitest.mutationtest;

import java.util.Collection;
import java.util.List;

import org.pitest.mutationtest.engine.MutationDetails;

/**
 * Chooses a start status for a mutation
 */
public interface MutationAnalyser {

  List<MutationResult> analyse(Collection<MutationDetails> mutationsForClasses);

}
