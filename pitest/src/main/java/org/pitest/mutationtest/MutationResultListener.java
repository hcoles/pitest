package org.pitest.mutationtest;

public interface MutationResultListener {

  void runStart();

  void handleMutationResult(ClassMutationResults results);

  void runEnd();

}
