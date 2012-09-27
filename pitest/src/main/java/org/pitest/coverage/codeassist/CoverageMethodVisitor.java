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
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.boot.CodeCoverageStore;

/**
 * Adds probes to each line of a method
 */
public class CoverageMethodVisitor extends MethodVisitor {
  private final MethodVisitor        methodVisitor;
  private final int                  classId;
  private final CoverageClassVisitor cv;

  public CoverageMethodVisitor(final CoverageClassVisitor cv,
      final int classId, final MethodVisitor writer) {
    super(Opcodes.ASM4, writer);

    this.methodVisitor = writer;
    this.classId = classId;
    this.cv = cv;
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    final int probeId = this.cv.registerLine(line);
    this.methodVisitor.visitLdcInsn(CodeCoverageStore.encode(this.classId,
        probeId));
    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CLASS_NAME,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC);
    this.methodVisitor.visitLineNumber(line, start);
  }

}
