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
 * Instruments a method adding probes at each line. The strategy requires
 * the compiler to be configured to add line number debug information.
 * 
 * Probes are implemented by adding an array to each method. Lines hits
 * are registered by a write to this local array. Each method exit point
 * is then augmented with a call that passes this array to the coverage store
 * class that handles communication of this data back to the parent
 * process on the completion of each test.
 * 
 * All methods are wrapped in a try finally block to ensure that coverage data
 * is sent in the event of a runtime exception.
 * 
 * Creating a new array on each method entry is not cheap - other coverage
 * systems add a static field used across all methods. We must clear down
 * all coverage history for each test however. Resetting static fields in all loaded
 * classes would be messy to implement - it may or may not be faster than the current approach.
 */
public class CoverageMethodVisitor extends AdviceAdapter {
  private final MethodVisitor        methodVisitor;
  private final int                  classId;
  private final int                  numberOfLines;
  private final CoverageClassVisitor cv;
  
  /**
   * label to mark start of try finally block that is added to each method
   */
  private final Label                before    = new Label();
  
  /**
   * label to mark handler block of try finally
   */
  private final Label                handler   = new Label();
  

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
    pushConstant(this.numberOfLines);
                                                 
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
    // get probe id - unique within parent class
    final int probeId = this.cv.registerLine(line);
    
    this.probes.add(CodeCoverageStore.encode(this.classId, probeId));

      this.mv.visitVarInsn(ALOAD, this.probeArrayLocal);
      pushConstant(this.lineCount);
      pushConstant(probeId);
      this.mv.visitInsn(IASTORE);
      this.lineCount++;
    
    this.methodVisitor.visitLineNumber(line, start);
  }
  
  private void pushConstant(int value) {
    switch ( value ) {
    case 0:
      mv.visitInsn(ICONST_0);
      break;
    case 1:
      mv.visitInsn(ICONST_1);
      break;
    case 2:
      mv.visitInsn(ICONST_2);
      break;
    case 3:
      mv.visitInsn(ICONST_3);
      break;
    case 4:
      mv.visitInsn(ICONST_4);
      break;
    case 5:
      mv.visitInsn(ICONST_5);
      break;
      default :
        if ( value <= Byte.MAX_VALUE ) {
          mv.visitIntInsn(Opcodes.BIPUSH, value);
        } else if ( value <= Short.MAX_VALUE) {
          mv.visitIntInsn(Opcodes.SIPUSH, value);
        } else {
          mv.visitLdcInsn(value);
        }
    }
  }
  
}
