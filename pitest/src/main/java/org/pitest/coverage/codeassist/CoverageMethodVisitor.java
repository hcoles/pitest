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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import sun.pitest.CodeCoverageStore;

/**
 * Instruments a method adding probes at each line. The strategy requires the
 * compiler to be configured to add line number debug information.
 * 
 * Probes are implemented by adding an array to each method. Lines hits are
 * registered by a write to this local array. Each method exit point is then
 * augmented with a call that passes this array to the coverage store class that
 * handles communication of this data back to the parent process on the
 * completion of each test.
 * 
 * All methods are wrapped in a try finally block to ensure that coverage data
 * is sent in the event of a runtime exception.
 * 
 * Creating a new array on each method entry is not cheap - other coverage
 * systems add a static field used across all methods. We must clear down all
 * coverage history for each test however. Resetting static fields in all loaded
 * classes would be messy to implement - it may or may not be faster than the
 * current approach.
 */
public class CoverageMethodVisitor extends AdviceAdapter {
  private final MethodVisitor methodVisitor;
  private final int           classId;
  private final int           numberOfProbes;
  private final LineTracker   lineTracker;
  private final int           probeOffset;
  /**
   * label to mark start of try finally block that is added to each method
   */
  private final Label         before     = new Label();

  /**
   * label to mark handler block of try finally
   */
  private final Label         handler    = new Label();

  private int                 probeCount = 0;
  // private int probeArrayLocal;
  private int                 probeHitArrayLocal;

  public CoverageMethodVisitor(final LineTracker lineTracker,
      final int classId, final MethodVisitor writer, final int access,
      final String name, final String desc, final int numberOfLines,
      final int probeOffset) {
    super(Opcodes.ASM5, writer, access, name, desc);

    this.methodVisitor = writer;
    this.classId = classId;
    this.lineTracker = lineTracker;
    this.numberOfProbes = numberOfLines;
    this.probeOffset = probeOffset;
  }

  @Override
  public void visitCode() {
    super.visitCode();

    this.probeHitArrayLocal = newLocal(Type.getType("[Z"));

    pushConstant(this.numberOfProbes);
    this.mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
    this.mv.visitVarInsn(ASTORE, this.probeHitArrayLocal);

    this.mv.visitLabel(this.before);
  }

  @Override
  public void visitMaxs(final int maxStack, final int maxLocals) {

    this.mv.visitTryCatchBlock(this.before, this.handler, this.handler, null);
    this.mv.visitLabel(this.handler);

    generateProbeReportCode();

    this.mv.visitInsn(ATHROW);
    this.mv.visitMaxs(0, 0);
  }

  @Override
  protected void onMethodExit(final int opcode) {
    // generated catch block will handle any throws ending block
    if (opcode != ATHROW) {
      generateProbeReportCode();
    }
  }

  private void generateProbeReportCode() {

    pushConstant(this.classId);
    pushConstant(this.probeOffset);
    this.mv.visitVarInsn(ALOAD, this.probeHitArrayLocal);

    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CLASS_NAME, CodeCoverageStore.PROBE_METHOD_NAME,
        "(II[Z)V", false);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {

    this.lineTracker.registerLine(line);

    this.mv.visitVarInsn(ALOAD, this.probeHitArrayLocal);
    pushConstant(this.probeCount);
    pushConstant(1);
    this.mv.visitInsn(BASTORE);
    this.probeCount++;

    this.methodVisitor.visitLineNumber(line, start);
  }

  private void pushConstant(final int value) {
    switch (value) {
    case 0:
      this.mv.visitInsn(ICONST_0);
      break;
    case 1:
      this.mv.visitInsn(ICONST_1);
      break;
    case 2:
      this.mv.visitInsn(ICONST_2);
      break;
    case 3:
      this.mv.visitInsn(ICONST_3);
      break;
    case 4:
      this.mv.visitInsn(ICONST_4);
      break;
    case 5:
      this.mv.visitInsn(ICONST_5);
      break;
    default:
      if (value <= Byte.MAX_VALUE) {
        this.mv.visitIntInsn(Opcodes.BIPUSH, value);
      } else if (value <= Short.MAX_VALUE) {
        this.mv.visitIntInsn(Opcodes.SIPUSH, value);
      } else {
        this.mv.visitLdcInsn(value);
      }
    }
  }

}
