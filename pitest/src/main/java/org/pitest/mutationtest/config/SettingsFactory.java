package org.pitest.mutationtest.config;

import java.util.Collection;

import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.build.DefaultMutationGrouperFactory;
import org.pitest.mutationtest.build.MutationGrouper;
import org.pitest.mutationtest.build.MutationGrouperFactory;
import org.pitest.mutationtest.filter.CompoundFilterFactory;
import org.pitest.mutationtest.filter.MutationFilterFactory;
import org.pitest.process.DefaultJavaExecutableLocator;
import org.pitest.process.JavaExecutableLocator;
import org.pitest.process.KnownLocationJavaExecutableLocator;
import org.pitest.util.PitError;
import org.pitest.util.ResultOutputStrategy;

public class SettingsFactory {

  private final ReportOptions options;

  public SettingsFactory(final ReportOptions options) {
    this.options = options;
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
    for (final MutationEngineFactory each : PluginServices
        .findMutationEngines()) {
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
    Collection<? extends MutationGrouperFactory> groupers = PluginServices.findGroupers();
    if ( groupers.isEmpty() ) {
      return new DefaultMutationGrouperFactory();
    }
    if ( groupers.size() > 1) {
      throw new PitError("Multiple implementations of " + MutationGrouper.class.getName() + " detected on classpath");
    }
    return groupers.iterator().next();
  }
  
  private Iterable<MutationResultListenerFactory> findListeners() {
    final Iterable<? extends MutationResultListenerFactory> listeners = PluginServices
        .findListeners();
    final Collection<MutationResultListenerFactory> matches = FCollection
        .filter(listeners, nameMatches(this.options.getOutputFormats()));
    if (matches.size() < this.options.getOutputFormats().size()) {
      throw new PitError("Unknown listener requested");
    }
    return matches;
  }

  private static F<MutationResultListenerFactory, Boolean> nameMatches(
      final Iterable<String> outputFormats) {
    return new F<MutationResultListenerFactory, Boolean>() {
      public Boolean apply(final MutationResultListenerFactory a) {
        return FCollection.contains(outputFormats, equalsIgnoreCase(a.name()));
      }
    };
  }

  private static Predicate<String> equalsIgnoreCase(final String other) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return a.equalsIgnoreCase(other);
      }
    };
  }

  public MutationFilterFactory createMutationFilter() {
    Collection<? extends MutationFilterFactory> filters = PluginServices.findFilters();
    return new CompoundFilterFactory(filters);
  }

}
