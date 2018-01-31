package org.pitest.mutationtest;

import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.plugin.ClientClasspathPlugin;

public interface MutationEngineFactory extends ClientClasspathPlugin {

  MutationEngine createEngine(EngineArguments arguments);

  String name();

}
