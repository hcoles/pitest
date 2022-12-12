package org.pitest.mutationtest.tdghistory;

import java.net.URL;
import java.util.Collection;
import java.util.Map;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
public interface TdgHistoryStore {
    void init();
    void recordClassHash();
    void recordResult(MutationResult result);
    Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults();
    Map<URL, String> getHistorySha();


    
}
