package org.pitest.mutationtest.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.IsolationUtils;
import org.pitest.util.ServiceLoader;

public class PluginServices {

  private final ClassLoader loader;

  public PluginServices(ClassLoader loader) {
    this.loader = loader;
  }

  public static PluginServices makeForContextLoader() {
    return new PluginServices(IsolationUtils.getContextClassLoader());
  }

  /**
   * Lists all plugin classes that must be present on the classpath of the
   * controlling process only.
   *
   * @return list of plugins
   */
  public Iterable<? extends ToolClasspathPlugin> findToolClasspathPlugins() {
    final List<ToolClasspathPlugin> l = new ArrayList<ToolClasspathPlugin>();
    l.addAll(findListeners());
    l.addAll(findGroupers());
    l.addAll(findTestPrioritisers());
    l.addAll(findInterceptors());
    return l;
  }

  /**
   * Lists all plugin classes that must be present on the classpath of the code
   * under test at runtime
   */
  public Iterable<? extends ClientClasspathPlugin> findClientClasspathPlugins() {
    final List<ClientClasspathPlugin> l = new ArrayList<ClientClasspathPlugin>();
    l.addAll(findMutationEngines());
    l.addAll(findTestFrameworkPlugins());
    l.addAll(nullPlugins());
    return l;
  }
  Collection<? extends TestPluginFactory> findTestFrameworkPlugins() {
    return ServiceLoader.load(TestPluginFactory.class, this.loader);
  }

  Collection<? extends MutationGrouperFactory> findGroupers() {
    return ServiceLoader.load(MutationGrouperFactory.class, this.loader);
  }

  Collection<? extends MutationResultListenerFactory> findListeners() {
    return ServiceLoader.load(MutationResultListenerFactory.class, this.loader);
  }

  Collection<? extends MutationEngineFactory> findMutationEngines() {
    return ServiceLoader.load(MutationEngineFactory.class, this.loader);
  }

  Collection<? extends TestPrioritiserFactory> findTestPrioritisers() {
    return ServiceLoader.load(TestPrioritiserFactory.class, this.loader);
  }

  private Collection<ClientClasspathPlugin> nullPlugins() {
    return ServiceLoader.load(ClientClasspathPlugin.class, this.loader);
  }

  public Collection<? extends MutationInterceptorFactory> findInterceptors() {
    return ServiceLoader.load(MutationInterceptorFactory.class, this.loader);
  }

}
