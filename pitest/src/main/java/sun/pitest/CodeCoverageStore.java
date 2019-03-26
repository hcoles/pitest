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
import java.util.Arrays;
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

  public static final String PROBE_FIELD_NAME                    = "$$pitCoverageProbes";
  public static final String PROBE_LENGTH_FIELD_NAME             = "$$pitCoverageProbeSize";

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

  public static synchronized void reset() {
    for (final Entry<Integer, boolean[]> each : CLASS_HITS.entrySet()) {
      if (each.getValue()[0]) { //Probe 0 gets covered by any method that runs
        Arrays.fill(each.getValue(), false);
      }
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
          blockHits.add(encode(classId, probeId));
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

  public static boolean[] getOrRegisterClassProbes(final int classId,
      int probeCount) {
    boolean[] ret = CLASS_HITS.putIfAbsent(classId, new boolean[probeCount + 1]);
    if (ret == null) {
      return CLASS_HITS.get(classId);
    }
    /*
    It's possible that some other java agent has transformed this class, which has
    resulted in it getting more blocks. It seems like our intended behavior is to
    still collect coverage of these new synthetic blocks, so we need to
    make sure that our coverage array grows when the class is re-transformed,
    and it's possible that we have already instrumented the class, causing its
    coverage array to get set up at the wrong size.
     */
    if (ret.length < probeCount + 1) {
      synchronized (CLASS_HITS) {
        ret = CLASS_HITS.get(classId);
        if (ret.length < probeCount + 1) {
          ret = new boolean[probeCount + 1];
          CLASS_HITS.put(classId, ret);
          return ret;
        }
      }
    }
    return ret;
  }

  public static void resetAllStaticState() {
    CLASS_HITS.clear();
  }

}
