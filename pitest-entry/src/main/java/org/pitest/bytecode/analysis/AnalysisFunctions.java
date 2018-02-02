package org.pitest.bytecode.analysis;

import java.util.function.Function;
import java.util.function.Predicate;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;

public class AnalysisFunctions {
  public static Function<MethodTree, Predicate<MutationDetails>> matchMutationsInMethod() {
    return new Function<MethodTree, Predicate<MutationDetails>>() {
      @Override
      public Predicate<MutationDetails> apply(final MethodTree method) {
        final Location methodLocation = method.asLocation();
        return new Predicate<MutationDetails>() {
          @Override
          public boolean test(MutationDetails a) {
            return methodLocation.equals(a.getId().getLocation());
          }
        };
      }
    };
  }
}
