package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.MutationResult;
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
    for (final MutationResult each : metaData.getMutations()) {
      this.historyStore.recordResult(each);
    }

  }

  @Override
  public void runEnd() {
    this.historyStore.close();
  }

}
