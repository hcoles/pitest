package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.PitError;
import org.pitest.coverage.export.CoverageExporter;
import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.report.ResultOutputStrategy;
import org.pitest.util.ServiceLoader;

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
    for (final MutationEngineFactory each : ServiceLoader
        .load(MutationEngineFactory.class)) {
      if (each.name().equals(this.options.getMutationEngine())) {
        return each;
      }
    }
    throw new PitError("Could not load requested engine "
        + this.options.getMutationEngine());
  }
  
  public ListenerFactory createListener() {
    Iterable<ListenerFactory> listeners = ServiceLoader.load(ListenerFactory.class);
    Collection<ListenerFactory> matches = FCollection.filter(listeners, nameMatches(this.options.getOutputFormats()));
    if ( matches.size() < this.options.getOutputFormats().size()) {
      throw new PitError("Unknown listener requested");
    }
    return new CompoundListenerFactory(matches);
  }

  private static F<ListenerFactory, Boolean> nameMatches(final Iterable<String> outputFormats) {
    return new F<ListenerFactory, Boolean>() {
      public Boolean apply(ListenerFactory a) {
        return FCollection.contains(outputFormats, equalsIgnoreCase(a.name()));
      }  
    };
  }
  
  private static Predicate<String> equalsIgnoreCase(final String other) {
    return new Predicate<String>() {
      public Boolean apply(String a) {
        return a.equalsIgnoreCase(other);
      }
    };
  }



}
