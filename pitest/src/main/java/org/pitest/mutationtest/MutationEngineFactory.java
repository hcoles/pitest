package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.plugin.ClientClasspathPlugin;

public interface MutationEngineFactory extends ClientClasspathPlugin {

  MutationEngine createEngine(boolean mutateStaticInitializers,
      Predicate<String> excludedMethods,
      Collection<String> loggingClasses,
      Collection<String> mutators, boolean detectInlinedCode);

  String name();

}
