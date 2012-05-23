/*
 * Based on http://code.google.com/p/javacoveragent/ by
 * "alex.mq0" and "dmitry.kandalov"
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

package org.pitest.boot;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ivanalx
 */
public final class CodeCoverageStore {

  public static final String    CODE_COVERAGE_CALCULATOR_CLASS_NAME       = CodeCoverageStore.class
                                                                              .getName()
                                                                              .replace(
                                                                                  '.',
                                                                                  '/');
  public static final String    CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME = "visitLine";
  public static final String    CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC = "(J)V";

  private static InvokeReceiver invokeQueue;
  private static int            classId                                   = 0;

  private static Set<Long>      lineHits                                  = new HashSet<Long>();

  public static void init(final InvokeReceiver invokeQueue) {
    CodeCoverageStore.invokeQueue = invokeQueue;
  }

  private CodeCoverageStore() {
  }

  public synchronized static void visitLine(final long lineId) { // NO_UCD
    lineHits.add(lineId);
  }

  public synchronized static void reset() {
    lineHits = new HashSet<Long>();
  }

  public synchronized static Collection<Long> getHits() {
    return new ArrayList<Long>(lineHits);
  }

  public static int registerClass(final String className) {
    final int id = nextId();
    invokeQueue.registerClass(id, className);
    return id;
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

}
