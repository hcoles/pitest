package org.pitest.mutationtest;


public interface MutationResultListener {

  void runStart();

  void handleMutationResult(MutationMetaData metaData);

  void runEnd();

}
