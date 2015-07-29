package org.pitest.mutationtest.incremental;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.ClassHistory;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class NullHistoryStore implements HistoryStore {

  @Override
  public void initialize() {

  }

  @Override
  public void recordResult(final MutationResult result) {

  }

  @Override
  public Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults() {
    return Collections.emptyMap();
  }

  @Override
  public Map<ClassName, ClassHistory> getHistoricClassPath() {
    return Collections.emptyMap();
  }

  @Override
  public void recordClassPath(final Collection<HierarchicalClassId> ids,
      final CoverageDatabase coverageInfo) {

  }

}
