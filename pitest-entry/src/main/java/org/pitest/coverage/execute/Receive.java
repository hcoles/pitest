package org.pitest.coverage.execute;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.pitest.classinfo.ClassName;
import org.pitest.coverage.BlockLocation;
import org.pitest.coverage.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MethodName;
import org.pitest.testapi.Description;
import org.pitest.util.Id;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;

import sun.pitest.CodeCoverageStore;

final class Receive implements ReceiveStrategy {

  private final Map<Integer, ClassName>     classIdToName = new ConcurrentHashMap<>();
  private final Map<Long, BlockLocation>    probeToBlock  = new ConcurrentHashMap<>();

  private final SideEffect1<CoverageResult> handler;

  Receive(final SideEffect1<CoverageResult> handler) {
    this.handler = handler;
  }

  @Override
  public void apply(final byte control, final SafeDataInputStream is) {
    switch (control) {
    case Id.CLAZZ:
      final int id = is.readInt();
      final String name = is.readString();
      this.classIdToName.put(id, ClassName.fromString(name));
      break;
    case Id.PROBES:
      handleProbes(is);
      break;
    case Id.OUTCOME:
      handleTestEnd(is);
      break;
    case Id.DONE:
      // nothing to do ?
    }
  }

  private void handleProbes(final SafeDataInputStream is) {
    final int classId = is.readInt();
    final String methodName = is.readString();
    final String methodSig = is.readString();
    final int first = is.readInt();
    final int last = is.readInt();
    final Location loc = Location.location(this.classIdToName.get(classId),
        MethodName.fromString(methodName), methodSig);
    for (int i = first; i != (last + 1); i++) {
      // nb, convert from classwide id to method scoped index within
      // BlockLocation
      this.probeToBlock.put(CodeCoverageStore.encode(classId, i),
          new BlockLocation(loc, i - first));
    }
  }

  private void handleTestEnd(final SafeDataInputStream is) {
    final Description d = is.read(Description.class);
    final int numberOfResults = is.readInt();

    final Set<BlockLocation> hits = new HashSet<>(numberOfResults);

    for (int i = 0; i != numberOfResults; i++) {
      readProbeHit(is, hits);
    }

    this.handler.apply(createCoverageResult(is, d, hits));
  }

  private void readProbeHit(final SafeDataInputStream is,
      final Set<BlockLocation> hits) {
    final long encoded = is.readLong();
    final BlockLocation location = probeToBlock(encoded);
    hits.add(location);
  }

  private BlockLocation probeToBlock(long encoded) {
    return this.probeToBlock.get(encoded);
  }

  private CoverageResult createCoverageResult(final SafeDataInputStream is,
      final Description d, Collection<BlockLocation> visitedBlocks) {
    final boolean isGreen = is.readBoolean();
    final int executionTime = is.readInt();
    final CoverageResult cr = new CoverageResult(d, executionTime, isGreen,
        visitedBlocks);
    return cr;
  }

}