package org.pitest.mutationtest.build;

import java.util.concurrent.Callable;

import org.pitest.mutationtest.MutationMetaData;

/**
 * A unit of mutation analysis
 */
public interface MutationAnalysisUnit extends Callable<MutationMetaData> {

  public int priority();
  
}
