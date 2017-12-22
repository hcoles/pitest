package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.plugin.ClientClasspathPlugin;

public interface MutationEngineFactory extends ClientClasspathPlugin {

  MutationEngine createEngine(
      Collection<String> excludedMethods,
      Collection<String> mutators);

  String name();

}
