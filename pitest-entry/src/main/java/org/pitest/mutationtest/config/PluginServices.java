package org.pitest.mutationtest.config;

import org.pitest.classpath.CodeSourceFactory;
import org.pitest.coverage.CoverageExporterFactory;
import org.pitest.coverage.TestStatListenerFactory;
import org.pitest.mutationtest.HistoryFactory;
import org.pitest.mutationtest.build.CoverageTransformerFactory;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultInterceptor;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutatorInfo;
import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.environment.TransformationPlugin;
import org.pitest.mutationtest.verify.BuildVerifierFactory;
import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.ProvidesFeature;
import org.pitest.plugin.ToolClasspathPlugin;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.IsolationUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;


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
    l.addAll(findMutationResultInterceptor());
    l.addAll(findCoverageTransformers());
    l.addAll(findVerifiers());
    l.addAll(findCodeSources());
    l.addAll(findHistory());
    l.addAll(findCoverageExport());
    l.addAll(findStandAloneMutatorInfos());
    l.addAll(findTestStatListeners());
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
    l.addAll(load(TransformationPlugin.class));
    l.addAll(load(EnvironmentResetPlugin.class));
    return l;
  }

  public Iterable<? extends File> findClientClasspathPluginDescriptors() {
    final List<File> l = new ArrayList<>();
    l.addAll(findMutationEngineDescriptors());
    l.addAll(findMutationOperatorDescriptors());
    l.addAll(findTestFrameworkPluginDescriptors());
    l.addAll(nullPluginDescriptors());
    l.addAll(loader.findPluginDescriptors(TransformationPlugin.class));
    l.addAll(loader.findPluginDescriptors(EnvironmentResetPlugin.class));
    return l;
  }

  public Collection<? extends ConfigurationUpdater> findConfigurationUpdaters() {
    return load(ConfigurationUpdater.class);
  }

  public Collection<? extends MethodMutatorFactory> findMutationOperators() {
    return load(MethodMutatorFactory.class);
  }

  Collection<File> findMutationOperatorDescriptors() {
    return loader.findPluginDescriptors(MethodMutatorFactory.class);
  }

  Collection<? extends TestPluginFactory> findTestFrameworkPlugins() {
    return load(TestPluginFactory.class);
  }

  Collection<File> findTestFrameworkPluginDescriptors() {
    return loader.findPluginDescriptors(TestPluginFactory.class);
  }

  Collection<? extends MutationGrouperFactory> findGroupers() {
    return load(MutationGrouperFactory.class);
  }

  Collection<? extends MutationResultListenerFactory> findListeners() {
    return load(MutationResultListenerFactory.class);
  }

  Collection<? extends MutationEngineFactory> findMutationEngines() {
    return load(MutationEngineFactory.class);
  }

  Collection<File> findMutationEngineDescriptors() {
    return loader.findPluginDescriptors(MutationEngineFactory.class);
  }

  Collection<? extends TestPrioritiserFactory> findTestPrioritisers() {
    return load(TestPrioritiserFactory.class);
  }

  private Collection<ClientClasspathPlugin> nullPlugins() {
    return load(ClientClasspathPlugin.class);
  }

  private Collection<File> nullPluginDescriptors() {
    return loader.findPluginDescriptors(ClientClasspathPlugin.class);
  }

  public Collection<MutationInterceptorFactory> findInterceptors() {
    return load(MutationInterceptorFactory.class);
  }

  public List<BuildVerifierFactory> findVerifiers() {
    return new ArrayList<>(load(BuildVerifierFactory.class));
  }

  public List<MutationResultInterceptor> findMutationResultInterceptor() {
    return new ArrayList<>(load(MutationResultInterceptor.class));
  }

  public List<CoverageTransformerFactory> findCoverageTransformers() {
    return new ArrayList<>(load(CoverageTransformerFactory.class));
  }

  public List<CodeSourceFactory> findCodeSources() {
    return new ArrayList<>(load(CodeSourceFactory.class));
  }

  public List<HistoryFactory> findHistory() {
    return new ArrayList<>(load(HistoryFactory.class));
  }


  public List<CoverageExporterFactory> findCoverageExport() {
    return new ArrayList<>(load(CoverageExporterFactory.class));
  }

  public List<TestStatListenerFactory> findTestStatListeners() {
    return new ArrayList<>(load(TestStatListenerFactory.class));
  }

  public List<MutatorInfo> findMutatorInfos() {
    List<MutatorInfo> combined = findMutationOperators().stream()
            .filter(p -> p instanceof MutatorInfo)
            .map(MutatorInfo.class::cast)
            .collect(Collectors.toList());

    List<MutatorInfo> info = findStandAloneMutatorInfos();
    info.addAll(combined);
    return info;
  }

  List<MutatorInfo> findStandAloneMutatorInfos() {
    return new ArrayList<>(load(MutatorInfo.class));
  }

  public Collection<ProvidesFeature> findFeatures() {
    return findToolClasspathPlugins().stream()
            .filter(p -> p instanceof ProvidesFeature)
            .map(ProvidesFeature.class::cast)
            .collect(Collectors.toList());
  }

  private <S> Collection<S> load(final Class<S> ifc) {
      return loader.load(ifc);
  }

}
