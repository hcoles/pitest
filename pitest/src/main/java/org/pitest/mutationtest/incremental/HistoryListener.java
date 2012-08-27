package org.pitest.mutationtest.incremental;

import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.instrument.MutationMetaData;
import org.pitest.mutationtest.results.MutationResult;

public class HistoryListener implements MutationResultListener {
  
  private final HistoryStore historyStore;

  public HistoryListener(final HistoryStore historyStore) {
    this.historyStore = historyStore;
  }

  public void runStart() {

  }

  public void handleMutationResult(final MutationMetaData metaData) {
    for ( MutationResult each : metaData.getMutations() ) {
      historyStore.recordResult(each);
    }

  }

  public void runEnd() {

  }

}
