package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.MutationEngine;

public interface MutationConfigFactory {

  public MutationEngine createEngine(final boolean mutateStaticInitializers,
      final Predicate<String> excludedMethods,
      final Collection<String> loggingClasses,
      final Collection<String> mutators,
      final boolean detectInlinedCode);

}
