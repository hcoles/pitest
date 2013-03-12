package org.pitest.mutationtest;

import org.pitest.extension.TestUnit;

public interface MutationAnalysisUnit extends TestUnit {
  
  public int priority();

}
