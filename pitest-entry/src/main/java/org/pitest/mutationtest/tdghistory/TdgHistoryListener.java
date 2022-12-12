package org.pitest.mutationtest.tdghistory;
import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.tdghistory.TdgHistoryStore;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationResultListener;
public class TdgHistoryListener implements MutationResultListener {
    private TdgHistoryStore tdgHistory;
    public TdgHistoryListener(TdgHistoryStore tdgHistory) {
        this.tdgHistory = tdgHistory;
    }
    @Override
    public void runStart() {

    }

    @Override
    public void handleMutationResult(final ClassMutationResults metaData) {
        for (final MutationResult each : metaData.getMutations()) {
            this.tdgHistory.recordResult(each);
        }

  }

    @Override
    public void runEnd() {

    }
}
