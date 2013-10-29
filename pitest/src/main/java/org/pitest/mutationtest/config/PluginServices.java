package org.pitest.mutationtest.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.util.ServiceLoader;

public class PluginServices {

  /**
   * Lists all plugin classes that must be present on the classpath of the
   * controlling process only.
   */
  public static Iterable<? extends ToolClasspathPlugin> findToolClasspathPlugins() {
    final List<ToolClasspathPlugin> l = new ArrayList<ToolClasspathPlugin>();
    l.addAll(findListeners());
    l.addAll(findGroupers());
    l.addAll(findFilters());
    l.addAll(findTestPrioritisers());
    return l;
  }

  /**
   * Lists all plugin classes that must be present on the classpath of the code
   * under test at runtime
   */
  public static Iterable<? extends ClientClasspathPlugin> findClientClasspathPlugins() {
    final List<ClientClasspathPlugin> l = new ArrayList<ClientClasspathPlugin>();
    l.addAll(findMutationEngines());
    l.addAll(nullPlugins());
    return l;
  }
  
  static Collection<? extends MutationGrouperFactory> findGroupers() {
    return ServiceLoader.load(MutationGrouperFactory.class);
  }
  
  static Collection<? extends MutationFilterFactory> findFilters() {
    return ServiceLoader.load(MutationFilterFactory.class);
  }

  static Collection<? extends MutationResultListenerFactory> findListeners() {
    return ServiceLoader.load(MutationResultListenerFactory.class);
  }

  static Collection<? extends MutationEngineFactory> findMutationEngines() {
    return ServiceLoader.load(MutationEngineFactory.class);
  }

  static Collection<? extends TestPrioritiserFactory> findTestPrioritisers() {
    return ServiceLoader.load(TestPrioritiserFactory.class);
  }
  
  private static Collection<ClientClasspathPlugin> nullPlugins() {
    return ServiceLoader.load(ClientClasspathPlugin.class);
  }

}
