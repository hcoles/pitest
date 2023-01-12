package org.pitest.mutationtest.build;

import org.pitest.coverage.ReportCoverage;

/**
 * Allows modification of coverage prior to reporting
 */
public interface CoverageTransformer {
    ReportCoverage transform(ReportCoverage in);
}
