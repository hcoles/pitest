package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.plugin.ClientClasspathPlugin;

public interface MutationEngineFactory extends ClientClasspathPlugin {

  MutationEngine createEngine(final boolean mutateStaticInitializers,
      final Predicate<String> excludedMethods,
      final Collection<String> loggingClasses,
      final Collection<String> mutators, final boolean detectInlinedCode);

  String name();

}
