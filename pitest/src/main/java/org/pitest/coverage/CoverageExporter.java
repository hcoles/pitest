package org.pitest.coverage;

import java.util.Collection;

public interface CoverageExporter {

  void recordCoverage(Collection<BlockCoverage> coverage);

}