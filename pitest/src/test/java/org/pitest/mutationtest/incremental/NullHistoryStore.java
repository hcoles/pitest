package org.pitest.mutationtest.incremental;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.MutationResult;

public class NullHistoryStore implements HistoryStore {

  public void initialize() {

  }


  public void recordResult(final MutationResult result) {

  }

  public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
    return Collections.emptyMap();
  }

  public Map<ClassName, ClassHistory> getHistoricClassPath() {
    return Collections.emptyMap();
  }

  public void recordClassPath(Collection<HierarchicalClassId> ids,
      CoverageDatabase coverageInfo) {
    
  }

}
