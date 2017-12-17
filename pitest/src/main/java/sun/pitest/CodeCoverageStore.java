/*
 * Originally based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov" - but don't think anything of the original
 * now remains in terms of either code or design.
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

// placed in a sun package so non delegating classloaders are likely
// to still delegate it's loading
package sun.pitest;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Store for line visit information.
 */
public final class CodeCoverageStore {

  private static final int                     CLASS_HIT_INDEX   = 0;

  public static final String                   CLASS_NAME        = CodeCoverageStore.class
                                                                     .getName()
                                                                     .replace(
                                                                         '.',
                                                                         '/');
  public static final String                   PROBE_METHOD_NAME = "visitProbes";

  private static InvokeReceiver                invokeQueue;
  private static int                           classId           = 0;

  // array of probe hits, first slot indicates any hits to the class.
  // testing suggests boolean array with synchronization to ensure happens
  // before relationship significantly outperforms
  // both AtomicInteger array with bit per flag and integer per flag.
  // optimisation with other methods of ensuring a happens before not yet
  // investigated
  private static final Map<Integer, boolean[]> CLASS_HITS        = new ConcurrentHashMap<>();

  public static void init(final InvokeReceiver invokeQueue) {
    CodeCoverageStore.invokeQueue = invokeQueue;
  }

  private CodeCoverageStore() {
  }

  public static void visitSingleProbe(final int classId, final int probe) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    bs[probe + 1] = true;
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean[] probes) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    for (int i = 0; i != probes.length; i++) {
      if (probes[i]) {
        bs[i + offset + 1] = true;
      }
    }
  }

  // ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
  //
  // Overloaded special case implementations for methods with 1 to N probes.
  // Allows probes to be implemented as
  // local variables and the loop in the array based version to be unrolled.
  //

  public static void visitProbes(final int classId, final int offset,
      final boolean p0) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8, final boolean p9) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
    if (p9) {
      bs[offset + 10] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8, final boolean p9, final boolean p10) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
    if (p9) {
      bs[offset + 10] = true;
    }
    if (p10) {
      bs[offset + 11] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8, final boolean p9, final boolean p10, final boolean p11) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
    if (p9) {
      bs[offset + 10] = true;
    }
    if (p10) {
      bs[offset + 11] = true;
    }
    if (p11) {
      bs[offset + 12] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8, final boolean p9, final boolean p10, final boolean p11,
      final boolean p12) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
    if (p9) {
      bs[offset + 10] = true;
    }
    if (p10) {
      bs[offset + 11] = true;
    }
    if (p11) {
      bs[offset + 12] = true;
    }
    if (p12) {
      bs[offset + 13] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8, final boolean p9, final boolean p10, final boolean p11,
      final boolean p12, final boolean p13) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
    if (p9) {
      bs[offset + 10] = true;
    }
    if (p10) {
      bs[offset + 11] = true;
    }
    if (p11) {
      bs[offset + 12] = true;
    }
    if (p12) {
      bs[offset + 13] = true;
    }
    if (p13) {
      bs[offset + 14] = true;
    }
  }

  public static void visitProbes(final int classId, final int offset,
      final boolean p0, final boolean p1, final boolean p2, final boolean p3,
      final boolean p4, final boolean p5, final boolean p6, final boolean p7,
      final boolean p8, final boolean p9, final boolean p10, final boolean p11,
      final boolean p12, final boolean p13, final boolean p14) { // NO_UCD
    final boolean[] bs = CLASS_HITS.get(classId);
    bs[CLASS_HIT_INDEX] = true;
    if (p0) {
      bs[offset + 1] = true;
    }
    if (p1) {
      bs[offset + 2] = true;
    }
    if (p2) {
      bs[offset + 3] = true;
    }
    if (p3) {
      bs[offset + 4] = true;
    }
    if (p4) {
      bs[offset + 5] = true;
    }
    if (p5) {
      bs[offset + 6] = true;
    }
    if (p6) {
      bs[offset + 7] = true;
    }
    if (p7) {
      bs[offset + 8] = true;
    }
    if (p8) {
      bs[offset + 9] = true;
    }
    if (p9) {
      bs[offset + 10] = true;
    }
    if (p10) {
      bs[offset + 11] = true;
    }
    if (p11) {
      bs[offset + 12] = true;
    }
    if (p12) {
      bs[offset + 13] = true;
    }
    if (p13) {
      bs[offset + 14] = true;
    }
    if (p14) {
      bs[offset + 15] = true;
    }
  }

  public static synchronized void reset() {
    for (final Entry<Integer, boolean[]> each : CLASS_HITS.entrySet()) {
      CLASS_HITS.put(each.getKey(), new boolean[each.getValue().length]);
    }
  }

  public static synchronized Collection<Long> getHits() {
    final Collection<Long> blockHits = new ArrayList<>();
    for (final Entry<Integer, boolean[]> each : CLASS_HITS.entrySet()) {
      final boolean[] bs = each.getValue();
      // first entry tracks if class has been visited at all
      if (!bs[CLASS_HIT_INDEX]) {
        continue;
      }
      final int classId = each.getKey();
      // final int[] mapping = classProbeToBlockMapping.get(classId);
      for (int probeId = 1; probeId != bs.length; probeId++) {
        if (bs[probeId]) {
          blockHits.add(encode(classId, probeId - 1));
        }
      }
    }
    return blockHits;
  }

  public static int registerClass(final String className) {
    final int id = nextId();
    invokeQueue.registerClass(id, className);
    return id;
  }

  public static void registerMethod(final int clazz, final String methodName,
      final String methodDesc, final int firstProbe, final int lastProbe) {
    invokeQueue.registerProbes(clazz, methodName, methodDesc, firstProbe,
        lastProbe);
  }

  private static synchronized int nextId() {
    return classId++;
  }

  public static int decodeClassId(final long value) {
    return (int) (value >> 32);
  }

  public static int decodeLineId(final long value) {
    return (int) (value & 0xFFFFFFFF);
  }

  public static long encode(final int classId, final int line) {
    return ((long) classId << 32) | line;
  }

  public static void registerClassProbes(final int classId, int probeCount) {
    CLASS_HITS.put(classId, new boolean[probeCount + 1]);
  }

  public static void resetAllStaticState() {
    CLASS_HITS.clear();
  }

}
