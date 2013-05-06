package org.pitest.mutationtest;

import org.pitest.PitError;
import org.pitest.coverage.export.CoverageExporter;
import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.coverage.export.NullCoverageExporter;
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

}
