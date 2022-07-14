package org.pitest.mutationtest.config;

import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.Feature;
import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.IsolationUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class PluginServices {

  private final Services loader;

  public PluginServices(Services loader) {
    this.loader = loader;
  }

  public static PluginServices makeForLoader(ClassLoader loader) {
    return new PluginServices(new ServicesFromClassLoader(loader));
  }

  public static PluginServices makeForContextLoader() {
    return makeForLoader(IsolationUtils.getContextClassLoader());
  }

  /**
   * Lists all plugin classes that must be present on the classpath of the
   * controlling process only.
   *
   * @return list of plugins
   */
  public Collection<? extends ToolClasspathPlugin> findToolClasspathPlugins() {
    final List<ToolClasspathPlugin> l = new ArrayList<>();
    l.addAll(findListeners());
    l.addAll(findGroupers());
    l.addAll(findTestPrioritisers());
    l.addAll(findInterceptors());
    l.addAll(findConfigurationUpdaters());
    return l;
  }

  /**
   * Lists all plugin classes that must be present on the classpath of the code
   * under test at runtime
   */
  public List<? extends ClientClasspathPlugin> findClientClasspathPlugins() {
    final List<ClientClasspathPlugin> l = new ArrayList<>();
    l.addAll(findMutationEngines());
    l.addAll(findMutationOperators());
    l.addAll(findTestFrameworkPlugins());
    l.addAll(nullPlugins());
    return l;
  }

  public Collection<? extends ConfigurationUpdater> findConfigurationUpdaters() {
    return load(ConfigurationUpdater.class);
  }

  public Collection<? extends MethodMutatorFactory> findMutationOperators() {
    return load(MethodMutatorFactory.class);
  }

  Collection<? extends TestPluginFactory> findTestFrameworkPlugins() {
    return load(TestPluginFactory.class);
  }

  Collection<? extends MutationGrouperFactory> findGroupers() {
    return load(MutationGrouperFactory.class);
  }

  Collection<? extends MutationResultListenerFactory> findListeners() {
    return adjustMissingFeatures(load(MutationResultListenerFactory.class));
  }

  Collection<? extends MutationEngineFactory> findMutationEngines() {
    return load(MutationEngineFactory.class);
  }

  Collection<? extends TestPrioritiserFactory> findTestPrioritisers() {
    return load(TestPrioritiserFactory.class);
  }

  private Collection<ClientClasspathPlugin> nullPlugins() {
    return load(ClientClasspathPlugin.class);
  }

  public Collection<MutationInterceptorFactory> findInterceptors() {
    return adjustMissingFeatures(load(MutationInterceptorFactory.class));
  }

  public Collection<ProvidesFeature> findFeatures() {
    return findToolClasspathPlugins().stream()
            .filter(p -> p instanceof ProvidesFeature)
            .map(ProvidesFeature.class::cast)
            .collect(Collectors.toList());
  }

  private <T extends ProvidesFeature> Collection<T> adjustMissingFeatures(Collection<T> allPlugins) {
    // Some features are 'missing', just placeholders to features that
    // can be provided by an external plugin. We list them so it is
    // clear that they are available. When the external plugin is present
    // the missing feature it implements must be removed.

    Map<Feature, List<T>> missing = allPlugins.stream()
            .filter(f -> f.provides().isMissing())
            .collect(Collectors.groupingBy(f -> f.provides()));

    Map<Feature, List<T>> real = allPlugins.stream()
            .filter(f -> !f.provides().isMissing())
            .collect(Collectors.groupingBy(f -> f.provides()));

    Stream<T> notImplemented = missing.entrySet().stream()
            .filter(e -> real.get(e.getKey()) == null)
            .flatMap(e -> e.getValue().stream());

    return Stream.concat(real.values().stream()
                    .flatMap(v -> v.stream()), notImplemented)
            .collect(Collectors.toList());
  }

  private <S> Collection<S> load(final Class<S> ifc) {
      return loader.load(ifc);
  }

}
