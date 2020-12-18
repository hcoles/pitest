package org.pitest.mutationtest.config;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
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
import org.pitest.util.PitError;
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
    final List<ToolClasspathPlugin> l = new ArrayList<>();
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
    final List<ClientClasspathPlugin> l = new ArrayList<>();
    l.addAll(findMutationEngines());
    l.addAll(findTestFrameworkPlugins());
    l.addAll(nullPlugins());
    return l;
  }

  public Iterable<? extends File> findClientClasspathPluginDescriptors() {
    final List<File> l = new ArrayList<>();
    l.addAll(findMutationEngineDescriptors());
    l.addAll(findTestFrameworkPluginDescriptors());
    l.addAll(nullPluginDescriptors());
    return l;
  }

  Collection<? extends TestPluginFactory> findTestFrameworkPlugins() {
    return ServiceLoader.load(TestPluginFactory.class, this.loader);
  }

  Collection<File> findTestFrameworkPluginDescriptors() {
    return findPluginDescriptors(TestPluginFactory.class);
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

  Collection<File> findMutationEngineDescriptors() {
    return findPluginDescriptors(MutationEngineFactory.class);
  }

  Collection<? extends TestPrioritiserFactory> findTestPrioritisers() {
    return ServiceLoader.load(TestPrioritiserFactory.class, this.loader);
  }

  private Collection<ClientClasspathPlugin> nullPlugins() {
    return ServiceLoader.load(ClientClasspathPlugin.class, this.loader);
  }

  private Collection<File> nullPluginDescriptors() {
    return findPluginDescriptors(ClientClasspathPlugin.class);
  }

  public Collection<? extends MutationInterceptorFactory> findInterceptors() {
    return ServiceLoader.load(MutationInterceptorFactory.class, this.loader);
  }

  private Collection<File> findPluginDescriptors(Class<?> ifc) {
    try {
      final Collection<File> pluginDescriptors = new ArrayList<>();
      Enumeration<URL> e = this.loader.getResources("META-INF/services/" + ifc.getName());
      while (e.hasMoreElements()) {
        URL url = e.nextElement();
        if (url.getProtocol() == "file") {
          pluginDescriptors.add(Paths.get(url.toURI()).getParent().getParent().getParent().toFile());
        }
      }
      return pluginDescriptors;
    } catch (final IOException | URISyntaxException ex) {
      throw new PitError("Error finding plugin descriptor for " + ifc.getName(), ex);
    }
  }

}
