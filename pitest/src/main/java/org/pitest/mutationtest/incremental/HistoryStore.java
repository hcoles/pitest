package org.pitest.mutationtest.incremental;

import java.util.Collection;
import java.util.Map;

import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.HierarchicalClassId;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.execute.MutationStatusTestPair;

public interface HistoryStore {

  void initialize();

  void recordClassPath(final Collection<HierarchicalClassId> ids,
      final CoverageDatabase coverageInfo);

  void recordResult(final MutationResult result);

  Map<MutationIdentifier, MutationStatusTestPair> getHistoricResults();

  Map<ClassName, ClassHistory> getHistoricClassPath();

}
