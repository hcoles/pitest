package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.mutationtest.results.MutationResult;

public interface MutationAnalyser {

  Collection<MutationResult> analyse(
      Collection<MutationDetails> mutationsForClasses);

}
