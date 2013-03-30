package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

public interface MutationConfigFactory {

  public MutationEngine createEngine(final boolean mutateStaticInitializers,
      final Predicate<String> excludedMethods,
      final Collection<String> loggingClasses,
      final Collection<? extends MethodMutatorFactory> mutators,
      final boolean detectInlinedCode);

}
