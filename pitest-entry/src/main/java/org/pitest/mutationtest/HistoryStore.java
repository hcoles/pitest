package org.pitest.mutationtest;

import java.util.Collection;
import java.util.Map;

import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.engine.MutationIdentifier;

public interface HistoryStore {

  void initialize();

  void recordClassPath(Collection<HierarchicalClassId> ids, CoverageDatabase coverageInfo);

  void recordResult(MutationResult result);

  Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults();

  Map<ClassName, ClassHistory> getHistoricClassPath();

}
