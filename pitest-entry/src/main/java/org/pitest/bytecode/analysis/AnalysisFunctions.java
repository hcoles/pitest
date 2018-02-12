package org.pitest.bytecode.analysis;

import java.util.function.Function;
import java.util.function.Predicate;

import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;

public class AnalysisFunctions {
  public static Function<MethodTree, Predicate<MutationDetails>> matchMutationsInMethod() {
    return method -> {
      final Location methodLocation = method.asLocation();
      return a -> methodLocation.equals(a.getId().getLocation());
    };
  }
}
