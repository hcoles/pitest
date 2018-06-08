package org.pitest.maven.report;

/**
 * Common code for report aggregation mojo.
 */
abstract class AbstractPitAggregationReportMojo extends PitReportMojo {

  static final String MUTATION_RESULT_FILTER = "target/pit-reports/mutations.xml";
  static final String LINECOVERAGE_FILTER    = "target/pit-reports/linecoverage.xml";

}
