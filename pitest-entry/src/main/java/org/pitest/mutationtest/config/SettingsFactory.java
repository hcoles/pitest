package org.pitest.mutationtest.config;

import org.pitest.classpath.CodeSource;
import org.pitest.classpath.CodeSourceFactory;
import org.pitest.classpath.DefaultCodeSource;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.CompoundCoverageExporterFactory;
import org.pitest.coverage.CoverageExporter;
import org.pitest.mutationtest.HistoryFactory;
import org.pitest.mutationtest.build.CoverageTransformer;
import org.pitest.mutationtest.build.CoverageTransformerFactory;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.CompoundMutationResultInterceptor;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.CompoundInterceptorFactory;
import org.pitest.mutationtest.build.DefaultMutationGrouperFactory;
import org.pitest.mutationtest.build.DefaultTestPrioritiserFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
import org.pitest.mutationtest.incremental.DefaultHistoryFactory;
import org.pitest.mutationtest.verify.BuildVerifierFactory;
import org.pitest.mutationtest.verify.CompoundBuildVerifierFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParser;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;
import org.pitest.plugin.ProvidesFeature;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaExecutableLocator;
import org.pitest.process.KnownLocationJavaExecutableLocator;
import org.pitest.util.PitError;
import org.pitest.util.ResultOutputStrategy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class SettingsFactory {

  private final ReportOptions  options;
  private final PluginServices plugins;

  public SettingsFactory(final ReportOptions options,
      final PluginServices plugins) {
    this.options = options;
    this.plugins = plugins;
  }

  public ResultOutputStrategy getOutputStrategy() {
    return this.options.getReportDirectoryStrategy();
  }

  public CoverageExporter createCoverageExporter() {
    if (this.options.shouldExportLineCoverage()) {
      final FeatureParser parser = new FeatureParser();
      return new CompoundCoverageExporterFactory(parser.parseFeatures(this.options.getFeatures()), this.plugins.findCoverageExport())
                .create(this.options.getReportDirectoryStrategy());
    } else {
      return new NullCoverageExporter();
    }
  }

  public MutationEngineFactory createEngine() {
    for (final MutationEngineFactory each : this.plugins.findMutationEngines()) {
      if (each.name().equals(this.options.getMutationEngine())) {
        return each;
      }
    }
    throw new PitError("Could not load requested engine "
        + this.options.getMutationEngine());
  }

  public MutationResultListenerFactory createListener() {
    final FeatureParser parser = new FeatureParser();
    return new CompoundListenerFactory(parser.parseFeatures(this.options.getFeatures()), findListeners());
  }

  public ConfigurationUpdater createUpdater() {
    final FeatureParser parser = new FeatureParser();
    return new CompoundConfigurationUpdater(parser.parseFeatures(this.options.getFeatures()), new ArrayList<>(plugins.findConfigurationUpdaters()));
  }

  public JavaExecutableLocator getJavaExecutable() {
    if (this.options.getJavaExecutable() != null) {
      return new KnownLocationJavaExecutableLocator(
          this.options.getJavaExecutable());
    }
    return new DefaultJavaExecutableLocator();
  }

  public MutationGrouperFactory getMutationGrouper() {
    // Grouping behaviour is important. We cannot have more than 1 class mutated within
    // a JVM or else the last mutation will poison the next. This restriction can only
    // be removed if the hotswap functionality is reworked.
    // Grouping behaviour is therefore hard coded for now.
    return new DefaultMutationGrouperFactory();
  }

  public CodeSource createCodeSource(ProjectClassPaths classPath) {
    List<CodeSourceFactory> sources = this.plugins.findCodeSources();
    if (sources.isEmpty()) {
      return new DefaultCodeSource(classPath);
    }
    if (sources.size() > 1) {
       throw new RuntimeException("More than one CodeSource found on classpath.");
    }
    return sources.get(0).createCodeSource(classPath);
  }

  public HistoryFactory createHistory() {
    List<HistoryFactory> available = this.plugins.findHistory();

    final FeatureParser parser = new FeatureParser();
    FeatureSelector<HistoryFactory> historyFeatures = new FeatureSelector<>(parser.parseFeatures(this.options.getFeatures()), available);
    List<HistoryFactory> enabledHistory = historyFeatures.getActiveFeatures();

    if (enabledHistory.isEmpty()) {
      return new DefaultHistoryFactory();
    }
    if (enabledHistory.size() > 1) {
      throw new RuntimeException("More than one HistoryFactory enabled.");
    }
    return enabledHistory.get(0);
  }

  public void describeFeatures(Consumer<Feature> enabled, Consumer<Feature> disabled) {
    final FeatureParser parser = new FeatureParser();
    final Collection<ProvidesFeature> available = new ArrayList<>(this.plugins.findFeatures());
    final List<FeatureSetting> settings = parser.parseFeatures(this.options.getFeatures());
    final FeatureSelector<ProvidesFeature> selector = new FeatureSelector<>(settings, available);

    List<Feature> enabledFeatures = selector.getActiveFeatures().stream()
      .map(toFeature())
      .filter(f -> !f.isInternal())
      .distinct()
      .sorted(byName())
      .collect(Collectors.toList());
      
    enabledFeatures.forEach(enabled);

    available.stream()
      .map(toFeature())
      .filter(f -> !f.isInternal())
      .distinct()
      .sorted(byName())
      .filter(f -> !enabledFeatures.contains(f))
      .forEach(disabled);
  }

  public void checkRequestedFeatures() {
    FeatureParser parser = new FeatureParser();
    Set<String> available = this.plugins.findFeatures().stream()
            .map(f -> f.provides().name().toUpperCase())
            .collect(Collectors.toSet());

    Optional<FeatureSetting> unknown = parser.parseFeatures(this.options.getFeatures()).stream()
            .filter(f -> !available.contains(f.feature().toUpperCase()))
            .findAny();

    unknown.ifPresent(setting -> {
      throw new IllegalArgumentException("Unknown feature " + setting.feature());
    });
  }

  public TestPrioritiserFactory getTestPrioritiser() {
    final Collection<? extends TestPrioritiserFactory> testPickers = this.plugins
        .findTestPrioritisers();
    return firstOrDefault(testPickers, new DefaultTestPrioritiserFactory());
  }

  public CoverageOptions createCoverageOptions() {
    return new CoverageOptions(
        this.options.getTargetClasses(), this.options.getExcludedClasses(),
        this.options.createMinionSettings(), this.options.getVerbosity());
  }

  public CompoundInterceptorFactory getInterceptor() {
    final Collection<? extends MutationInterceptorFactory> interceptors = this.plugins
        .findInterceptors();
    final FeatureParser parser = new FeatureParser();
    return new CompoundInterceptorFactory(parser.parseFeatures(this.options.getFeatures()), new ArrayList<>(interceptors));
  }

  public BuildVerifierFactory createVerifier() {
    return new CompoundBuildVerifierFactory(this.plugins.findVerifiers());
  }

  public CompoundMutationResultInterceptor getResultInterceptor() {
    return new CompoundMutationResultInterceptor(this.plugins.findMutationResultInterceptor());
  }

  public CoverageTransformer createCoverageTransformer(CodeSource code) {
    List<CoverageTransformerFactory> transformers = this.plugins.findCoverageTransformers();
    if (transformers.size() > 1) {
      throw new RuntimeException("More than 1 coverage transformer found on classpath");
    }
    if (transformers.isEmpty()) {
      return cov -> cov;
    }
    return transformers.get(0).create(code);
  }

  private Collection<MutationResultListenerFactory> findListeners() {
    final Iterable<? extends MutationResultListenerFactory> listeners = this.plugins
        .findListeners();
    final Collection<MutationResultListenerFactory> matches = FCollection
        .filter(listeners, nameMatches(this.options.getOutputFormats()));
    if (matches.size() < this.options.getOutputFormats().size()) {
      throw new PitError("Unknown listener requested in "
          + String.join(",", this.options.getOutputFormats()));
    }
    return matches;
  }

  private static Predicate<MutationResultListenerFactory> nameMatches(
          final Iterable<String> outputFormats) {
    // plugins can be either activated here by name
    // or later via the feature mechanism
    return a -> FCollection.contains(outputFormats, equalsIgnoreCase(a.name()))
            || !a.provides().equals(MutationResultListenerFactory.LEGACY_MODE);
  }


  private static Predicate<String> equalsIgnoreCase(final String other) {
    return a -> a.equalsIgnoreCase(other);
  }

  private static <T> T firstOrDefault(final Collection<? extends T> found,
      final T defaultInstance) {
    if (found.isEmpty()) {
      return defaultInstance;
    }
    if (found.size() > 1) {
      throw new PitError(
          "Multiple implementations of plugin detected on classpath");
    }
    return found.iterator().next();
  }

  private static Function<ProvidesFeature, Feature> toFeature() {
    return ProvidesFeature::provides;
  }


  private Comparator<Feature> byName() {
    return Comparator.comparing(Feature::name);
  }

}
