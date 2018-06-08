package org.pitest.maven.report;

import java.util.List;
import java.util.Locale;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.project.MavenProject;

/**
 * Goal which aggregates the results of multiple tests into a single result.
 *
 * <p>
 * Based upon Jacoco's ReportAggregateMojo, creates a structured report (HTML,
 * XML, or CSV) from multiple projects. The report is created the all modules
 * this project includes as dependencies.
 * </p>
 *
 * <p>
 * To successfully aggregate reports, each of the individual sub-module reports
 * must have the exportLineCoverage set to <code>true</code>, and must export an
 * XML formatted report.
 *
 * then call the mojo ONLY on the parent
 *
 * mvn pitest:report-aggregate-module --non-recursive
 * </p>
 *
 */
@Mojo(name = "report-aggregate-module", defaultPhase = LifecyclePhase.PREPARE_PACKAGE, aggregator = true)
public class PitReportAggregationModuleMojo extends AbstractPitAggregationReportMojo {

  @Override
  public String getDescription(final Locale locale) {
    return getName(locale) + " Coverage Report.";
  }

  @Override
  List<MavenProject> findDependencies() {
    return getProject().getCollectedProjects();
  }
}
