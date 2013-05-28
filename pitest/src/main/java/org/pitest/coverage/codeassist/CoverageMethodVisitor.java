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

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.pitest.boot.CodeCoverageStore;

/**
 * Adds probes to each line of a method
 */
public class CoverageMethodVisitor extends AdviceAdapter {
  private final MethodVisitor        methodVisitor;
  private final int                  classId;
  private final int                  numberOfLines;
  private final CoverageClassVisitor cv;
  private final Label                handler   = new Label();
  private final Label                before    = new Label();

  int                                lineCount = 0;
  int                                probeArrayLocal;

  private final List<Long>           probes    = new ArrayList<Long>();

  public CoverageMethodVisitor(final CoverageClassVisitor cv,
      final int classId, final MethodVisitor writer, final int access,
      final String name, final String desc, final int numberOfLines) {
    super(Opcodes.ASM4, writer, access, name, desc);

    this.methodVisitor = writer;
    this.classId = classId;
    this.cv = cv;
    this.numberOfLines = numberOfLines;
  }

  @Override
  public void visitCode() {
    super.visitCode();
    this.probeArrayLocal = newLocal(Type.getType("[I"));
    this.mv.visitIntInsn(SIPUSH, this.numberOfLines);
                                                 
    this.mv.visitIntInsn(NEWARRAY, T_INT);
    this.mv.visitVarInsn(ASTORE, this.probeArrayLocal);

    this.mv.visitLabel(this.before);
  }

  @Override
  public void visitMaxs(final int maxStack, final int maxLocals) {

    this.mv.visitTryCatchBlock(this.before, this.handler, this.handler, null);
    this.mv.visitLabel(this.handler);

    this.mv.visitInsn(ATHROW);
    this.mv.visitMaxs(maxStack, maxLocals);
  }

  @Override
  protected void onMethodExit(final int opcode) {
    generateProbeReportCode();
  }

  private void generateProbeReportCode() {
    
    mv.visitLdcInsn(this.classId);
    this.mv.visitVarInsn(ALOAD, this.probeArrayLocal);
 
    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CLASS_NAME,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CODE_METHOD_NAME,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CODE_METHOD_DESC);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    final int probeId = this.cv.registerLine(line);
    this.probes.add(CodeCoverageStore.encode(this.classId, probeId));

 
      this.mv.visitVarInsn(ALOAD, this.probeArrayLocal);
      this.mv.visitIntInsn(SIPUSH, this.lineCount);
      this.mv.visitIntInsn(SIPUSH, probeId);
      this.mv.visitInsn(IASTORE);
      this.lineCount++;
    
    this.methodVisitor.visitLineNumber(line, start);
  }
  
}
