package org.pitest.mutationtest.config;

import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.CompoundInterceptorFactory;
import org.pitest.mutationtest.build.DefaultMutationGrouperFactory;
import org.pitest.mutationtest.build.DefaultTestPrioritiserFactory;
import org.pitest.mutationtest.build.TdgTestPrioritiserFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
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
      return new DefaultCoverageExporter(getOutputStrategy());
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
    final Collection<? extends MutationGrouperFactory> groupers = this.plugins
        .findGroupers();
    return firstOrDefault(groupers, new DefaultMutationGrouperFactory());
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
    // return firstOrDefault(testPickers, new DefaultTestPrioritiserFactory());
    return firstOrDefault(testPickers, new TdgTestPrioritiserFactory());
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
