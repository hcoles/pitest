package org.pitest.coverage.execute;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


import org.pitest.coverage.ClassStatistics;
import org.pitest.coverage.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.testapi.Description;
import org.pitest.util.Id;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;

import sun.pitest.CodeCoverageStore;

final class Receive implements ReceiveStrategy {

  private final Map<Integer, String>        classIdToName = new ConcurrentHashMap<Integer, String>();
  private final SideEffect1<CoverageResult> handler;

  Receive(final SideEffect1<CoverageResult> handler) {
    this.handler = handler;
  }

  public void apply(final byte control, final SafeDataInputStream is) {
    switch (control) {
    case Id.CLAZZ:
      final int id = is.readInt();
      final String name = is.readString();
      this.classIdToName.put(id, name);
      break;
    case Id.OUTCOME:
      handleTestEnd(is);
      break;
    case Id.DONE:
      // nothing to do ?
    }
  }

  private void handleTestEnd(final SafeDataInputStream is) {
    final Description d = is.read(Description.class);
    final long numberOfResults = is.readLong();

    final Map<Integer, ClassStatistics> hits = new HashMap<Integer, ClassStatistics>();

    for (int i = 0; i != numberOfResults; i++) {
      readLineHit(is, hits);
    }

    this.handler.apply(createCoverageResult(is, d, hits));
  }

  private void readLineHit(final SafeDataInputStream is,
      final Map<Integer, ClassStatistics> hits) {
    final long encoded = is.readLong();
    final int classId = CodeCoverageStore.decodeClassId(encoded);
    final int lineNumber = CodeCoverageStore.decodeLineId(encoded);

    final ClassStatistics stats = getStatisticsForClass(hits, classId);

    stats.registerLineVisit(lineNumber);
  }

  private CoverageResult createCoverageResult(final SafeDataInputStream is,
      final Description d, final Map<Integer, ClassStatistics> hits) {
    final boolean isGreen = is.readBoolean();
    final int executionTime = is.readInt();
    final CoverageResult cr = new CoverageResult(d, executionTime, isGreen,
        hits.values());
    return cr;
  }

  private ClassStatistics getStatisticsForClass(
      final Map<Integer, ClassStatistics> hits, final int classId) {
    ClassStatistics stats = hits.get(classId);
    if (stats == null) {
      stats = new ClassStatistics(this.classIdToName.get(classId));
      hits.put(classId, stats);
    }
    return stats;
  }

}