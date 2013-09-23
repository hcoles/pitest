package org.pitest.mutationtest;

import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.util.ServiceLoader;

public class PluginServices {
  
  /**
   * Lists all plugin classes that must be present on the classpath of the
   * controlling process only. 
   */
  public static Iterable<? extends ToolClasspathPlugin> findToolClasspathPlugins() {
    return findListeners();
  }
  
  /**
   * Lists all plugin classes that must be present on the classpath of the code under test
   * at runtime 
   */
  public static Iterable<? extends ClientClasspathPlugin> findClientClasspathPlugins() {
    return findMutationEngines();
  }

  static Iterable<? extends ListenerFactory> findListeners() { 
    return ServiceLoader.load(ListenerFactory.class);
  }

  static Iterable<? extends MutationEngineFactory> findMutationEngines() {
    return ServiceLoader.load(MutationEngineFactory.class);
  }
  
  
}
