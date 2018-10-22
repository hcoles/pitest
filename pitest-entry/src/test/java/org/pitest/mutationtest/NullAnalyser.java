package org.pitest.mutationtest;

import java.util.Collection;
import java.util.function.Function;

import org.pitest.functional.FCollection;
import org.pitest.mutationtest.engine.MutationDetails;

/**
 * Always selects the start status for a mutation
 *
 */
public class NullAnalyser implements MutationAnalyser {

  @Override
  public Collection<MutationResult> analyse(
      final Collection<MutationDetails> mutationsForClasses) {
    return FCollection.map(mutationsForClasses, mutationToResult());
  }

  private Function<MutationDetails, MutationResult> mutationToResult() {
    return a -> new MutationResult(a, MutationStatusTestPair.notAnalysed(0,
        DetectionStatus.NOT_STARTED));
  }

}
