package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.MutationResultListener;

public class HistoryListener implements MutationResultListener {

  private final History historyStore;

  public HistoryListener(final History historyStore) {
    this.historyStore = historyStore;
  }

  @Override
  public void runStart() {

  }

  @Override
  public void handleMutationResult(final ClassMutationResults metaData) {
    // results are collected in an interceptor to ensure we have unmodified mutants
  }

  @Override
  public void runEnd() {
    this.historyStore.close();
  }

}
