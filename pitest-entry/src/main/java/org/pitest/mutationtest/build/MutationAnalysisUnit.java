package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.concurrent.Callable;

import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.engine.MutationDetails;

/**
 * A unit of mutation analysis
 */
public interface MutationAnalysisUnit extends Callable<MutationMetaData> {

  int priority();

  Collection<MutationDetails> mutants();
}
