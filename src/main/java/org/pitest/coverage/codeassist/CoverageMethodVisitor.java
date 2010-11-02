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

package org.pitest.coverage.codeassist;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.coverage.calculator.CodeCoverageStore;

/**
 * @author ivanalx
 * @date 26.01.2009 15:48:26
 */
public class CoverageMethodVisitor extends MethodAdapter {
  private final MethodVisitor methodVisitor;
  private final int           classId;
  private final int           methodId;

  private final boolean       allowMethodCoverage;
  private final boolean       allowLineCoverage;

  public CoverageMethodVisitor(final int classId, final MethodVisitor writer,
      final String name, final String methodDesc,
      final boolean allowMethodCoverage, final boolean allowLineCoverage) {
    super(writer);

    this.methodVisitor = writer;
    this.classId = classId;
    this.methodId = CodeCoverageStore.registerMethod(classId, name, methodDesc);
    this.allowLineCoverage = allowLineCoverage;
    this.allowMethodCoverage = allowMethodCoverage;
  }

  @Override
  public void visitCode() {
    if (this.allowMethodCoverage) {
      this.methodVisitor.visitLdcInsn(this.classId);
      this.methodVisitor.visitLdcInsn(this.methodId);
      this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
          CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CLASS_NAME,
          CodeCoverageStore.CODE_COVERAGE_CALCULATOR_METHOD_METHOD_NAME,
          CodeCoverageStore.CODE_COVERAGE_CALCULATOR_METHOD_METHOD_DESC);
    }
    this.methodVisitor.visitCode();
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    if (this.allowLineCoverage) {
      this.methodVisitor.visitLdcInsn(this.classId);
      this.methodVisitor.visitLdcInsn(line);
      this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
          CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CLASS_NAME,
          CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME,
          CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC);
    }
    this.methodVisitor.visitLineNumber(line, start);
  }
}
