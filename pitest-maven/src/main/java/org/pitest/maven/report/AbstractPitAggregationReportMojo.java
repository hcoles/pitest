package org.pitest.maven.report;

import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.util.List;

/**
 * Common code for report aggregation mojo.
 */
abstract class AbstractPitAggregationReportMojo extends PitReportMojo {

  static final String MUTATION_RESULT_FILTER = "target/pit-reports/mutations.xml";
  static final String LINECOVERAGE_FILTER    = "target/pit-reports/linecoverage.xml";

  /**
   * The projects in the reactor.
   */
  @Parameter(property = "reactorProjects", readonly = true)
  protected List<MavenProject> reactorProjects;
}
