package org.pitest.mutationtest.config;

import static org.pitest.functional.prelude.Prelude.not;

import java.util.Collection;

import org.pitest.classpath.ClassPathByteArraySource;
import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.DefaultMutationGrouperFactory;
import org.pitest.mutationtest.build.DefaultTestPrioritiserFactory;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.build.TestPrioritiserFactory;
import org.pitest.mutationtest.filter.CompoundFilterFactory;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaExecutableLocator;
import org.pitest.process.KnownLocationJavaExecutableLocator;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestPluginFactory;
import org.pitest.util.PitError;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.StringUtil;

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
    return new CompoundListenerFactory(findListeners());
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

  private Iterable<MutationResultListenerFactory> findListeners() {
    final Iterable<? extends MutationResultListenerFactory> listeners = this.plugins
        .findListeners();
    final Collection<MutationResultListenerFactory> matches = FCollection
        .filter(listeners, nameMatches(this.options.getOutputFormats()));
    if (matches.size() < this.options.getOutputFormats().size()) {
      throw new PitError("Unknown listener requested in "
          + StringUtil.join(this.options.getOutputFormats(), ","));
    }
    return matches;
  }

  private static F<MutationResultListenerFactory, Boolean> nameMatches(
      final Iterable<String> outputFormats) {
    return new F<MutationResultListenerFactory, Boolean>() {
      @Override
      public Boolean apply(final MutationResultListenerFactory a) {
        return FCollection.contains(outputFormats, equalsIgnoreCase(a.name()));
      }
    };
  }

  private static Predicate<String> equalsIgnoreCase(final String other) {
    return new Predicate<String>() {
      @Override
      public Boolean apply(final String a) {
        return a.equalsIgnoreCase(other);
      }
    };
  }

  public MutationFilterFactory createMutationFilter() {
    final Collection<? extends MutationFilterFactory> filters = this.plugins
        .findFilters();
    return new CompoundFilterFactory(filters);
  }

  public TestPrioritiserFactory getTestPrioritiser() {
    final Collection<? extends TestPrioritiserFactory> testPickers = this.plugins
        .findTestPrioritisers();
    return firstOrDefault(testPickers, new DefaultTestPrioritiserFactory());
  }

  public Configuration getTestFrameworkPlugin() {

    final Collection<? extends TestPluginFactory> testPlugins = this.plugins
        .findTestFrameworkPlugins();
    return firstOrDefault(testPlugins, new LegacyTestFrameworkPlugin())
        .createTestFrameworkConfiguration(this.options.getGroupConfig(),
            new ClassPathByteArraySource(this.options.getClassPath()));
  }

  @SuppressWarnings("unchecked")
  public CoverageOptions createCoverageOptions() {
    return new CoverageOptions(Prelude.and(
        this.options.getTargetClassesFilter(), not(commonClasses())),
        this.getTestFrameworkPlugin(), this.options.isVerbose(),
        this.options.getDependencyAnalysisMaxDistance());
  }

  private static F<String, Boolean> commonClasses() {
    return new F<String, Boolean>() {
      @Override
      public Boolean apply(final String name) {
        return name.startsWith("java") || name.startsWith("sun/")
            || name.startsWith("org/junit") || name.startsWith("junit")
            || name.startsWith("org/pitest/coverage")
            || name.startsWith("org/pitest/reloc")
            || name.startsWith("org/pitest/boot");
      }

    };
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

}
