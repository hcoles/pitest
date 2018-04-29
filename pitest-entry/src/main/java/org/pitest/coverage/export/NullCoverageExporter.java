package org.pitest.coverage.export;

import java.util.Collection;

import org.pitest.coverage.BlockCoverage;
import org.pitest.coverage.CoverageExporter;

public class NullCoverageExporter implements CoverageExporter {

  @Override
  public void recordCoverage(final Collection<BlockCoverage> coverage) {

  }

}
