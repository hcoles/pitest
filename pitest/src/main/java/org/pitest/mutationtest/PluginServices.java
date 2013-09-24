package org.pitest.mutationtest;

import java.util.ArrayList;
import java.util.Collection;

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
    ArrayList<ClientClasspathPlugin> l = new ArrayList<ClientClasspathPlugin>();
    l.addAll(findMutationEngines());
    l.addAll(nullPlugins());
    return l;
  }

  static Iterable<? extends MutationResultListenerFactory> findListeners() { 
    return ServiceLoader.load(MutationResultListenerFactory.class);
  }

  static Collection<? extends MutationEngineFactory> findMutationEngines() {
    return ServiceLoader.load(MutationEngineFactory.class);
  }
  
  private static Collection<ClientClasspathPlugin> nullPlugins() {
    return ServiceLoader.load(ClientClasspathPlugin.class);
  }
  
  
}
