package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.F;
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

  private F<MutationDetails, MutationResult> mutationToResult() {
    return new F<MutationDetails, MutationResult>() {

      @Override
      public MutationResult apply(final MutationDetails a) {
        return new MutationResult(a, new MutationStatusTestPair(0,
            DetectionStatus.NOT_STARTED));
      }

    };
  }

}
