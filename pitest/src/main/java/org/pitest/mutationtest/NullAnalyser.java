package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;

public class NullAnalyser implements MutationAnalyser {

  public Collection<MutationResult> analyse(
      Collection<MutationDetails> mutationsForClasses) {
    return FCollection.map(mutationsForClasses, mutationToResult());
  }

  private F<MutationDetails, MutationResult> mutationToResult() {
    return new F<MutationDetails, MutationResult>() {

      public MutationResult apply(MutationDetails a) {
        return new MutationResult(a, new MutationStatusTestPair(0,DetectionStatus.NOT_STARTED));
      }
      
    };
  }

}
