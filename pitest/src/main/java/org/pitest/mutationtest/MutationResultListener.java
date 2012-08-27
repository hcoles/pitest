package org.pitest.mutationtest;

import org.pitest.mutationtest.instrument.MutationMetaData;

public interface MutationResultListener {

  void runStart();

  void handleMutationResult(MutationMetaData metaData);

  void runEnd();

}
