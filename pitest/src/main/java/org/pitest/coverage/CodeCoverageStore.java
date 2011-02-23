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

package org.pitest.coverage;

import org.objectweb.asm.Type;
import org.objectweb.asm.commons.Method;

/**
 * @author ivanalx
 * @date 26.01.2009 14:41:04
 */
public final class CodeCoverageStore {
  public static final String        CODE_COVERAGE_CALCULATOR_CLASS_NAME;
  public static final String        CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME;
  public static final String        CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC;

  public static final String        CODE_COVERAGE_CALCULATOR_METHOD_METHOD_NAME;

  public static final String        CODE_COVERAGE_CALCULATOR_METHOD_METHOD_DESC;

  static {
    final Method addCalc = new Method("visitLine", Type.VOID_TYPE, new Type[] {
      Type.INT_TYPE, Type.INT_TYPE });
    final Method addCalcMethod = new Method("visitMethod", Type.VOID_TYPE,
        new Type[] { Type.INT_TYPE, Type.INT_TYPE });
    CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME = addCalc.getName();
    CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC = addCalc.getDescriptor();
    CODE_COVERAGE_CALCULATOR_CLASS_NAME = CodeCoverageStore.class.getName()
        .replace('.', '/');

    CODE_COVERAGE_CALCULATOR_METHOD_METHOD_NAME = addCalcMethod.getName();
    CODE_COVERAGE_CALCULATOR_METHOD_METHOD_DESC = addCalcMethod.getDescriptor();
  }

  private static InvokeQueue        invokeQueue;
  private static CoverageStatistics invokeStatistics;

  public static void init(final InvokeQueue invokeQueue,
      final CoverageStatistics invokeStatistics) {
    CodeCoverageStore.invokeQueue = invokeQueue;
    CodeCoverageStore.invokeStatistics = invokeStatistics;
  }

  private CodeCoverageStore() {
  }

  public static void visitLine(final int classId, final int codeLine) {
    invokeQueue.addCodelineInvoke(classId, codeLine);
  }

  public static int registerClass(final String className) {
    return invokeStatistics.registerClass(className);
  }

  public static InvokeQueue getInvokeQueue() {
    return invokeQueue;
  }

}
