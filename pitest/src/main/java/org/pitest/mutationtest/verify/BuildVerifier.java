package org.pitest.mutationtest.verify;

import org.pitest.coverage.CoverageDatabase;

public interface BuildVerifier {

  public void verify(CoverageDatabase coverageDatabase);

}
