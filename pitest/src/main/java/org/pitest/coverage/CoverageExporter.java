package org.pitest.coverage;

import java.util.Collection;

public interface CoverageExporter {

  public abstract void recordCoverage(Collection<BlockCoverage> coverage);

}