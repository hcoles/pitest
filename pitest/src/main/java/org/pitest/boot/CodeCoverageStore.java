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

/**
 * @author ivanalx
 * @date 26.01.2009 14:41:04
 */
public final class CodeCoverageStore {
  public static final String    CODE_COVERAGE_CALCULATOR_CLASS_NAME;
  public static final String    CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME;
  public static final String    CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC;

  static {

    CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME = "visitLine";// addCalc.getName();
    CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC = "(II)V";
    CODE_COVERAGE_CALCULATOR_CLASS_NAME = CodeCoverageStore.class.getName()
        .replace('.', '/');

  }

  private static InvokeReceiver invokeQueue;
  private static int            classId = 0;

  public static void init(final InvokeReceiver invokeQueue) {
    CodeCoverageStore.invokeQueue = invokeQueue;
  }

  private CodeCoverageStore() {
  }

  public static void visitLine(final int classId, final int codeLine) { // NO_UCD
    invokeQueue.addCodelineInvoke(classId, codeLine);
  }

  public static int registerClass(final String className) {
    final int id = nextId();
    invokeQueue.registerClass(id, className);
    return id;
  }

  private static synchronized int nextId() {
    return classId++;
  }

  public static void resetClassCounter() {
    classId = 0;
  }

}
