package org.pitest.mutationtest.tdghistory;
import java.util.Collections;
import java.util.Collection;
import java.util.Map;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import java.net.URL;
public class NullTdgHistoryStore implements TdgHistoryStore{
    @Override
    public void init() {

    }
    @Override
    public void recordClassHash() {

    }

    @Override
    public void recordResult(MutationResult result) {

    }

    @Override
    public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
        return Collections.emptyMap();
    }

    @Override
    public Map<URL, String> getHistorySha() {
        return Collections.emptyMap();
    }
    
}
