package org.pitest.mutationtest.build;

import org.pitest.testapi.TestUnit;

public interface MutationAnalysisUnit extends TestUnit {

  public int priority();

}
