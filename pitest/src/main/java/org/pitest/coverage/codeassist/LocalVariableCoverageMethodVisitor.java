package org.pitest.coverage.codeassist;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;
import org.pitest.boot.CodeCoverageStore;

public class LocalVariableCoverageMethodVisitor extends AdviceAdapter {
  private final MethodVisitor        methodVisitor;
  private final int                  classId;
  private final int                  numberOfProbes;
  private final CoverageClassVisitor cv;
  private final int probeOffset;

  /**
   * label to mark start of try finally block that is added to each method
   */
  private final Label                before    = new Label();

  /**
   * label to mark handler block of try finally
   */
  private final Label                handler   = new Label();

  int                                probeCount = 0;
  int locals[];//                                locals = new ArrayList<Integer>();

  

  public LocalVariableCoverageMethodVisitor(final CoverageClassVisitor cv,
      final int classId, final MethodVisitor writer, final int access,
      final String name, final String desc, final int numberOfLines, final int probeOffset) {
    super(Opcodes.ASM4, writer, access, name, desc);

    this.methodVisitor = writer;
    this.classId = classId;
    this.cv = cv;
    this.numberOfProbes = numberOfLines;
    this.probeOffset = probeOffset;
  }

  @Override
  public void visitCode() {
    super.visitCode();

    locals = new int[numberOfProbes];
    for ( int i = 0; i != numberOfProbes; i++) {
      locals[i]= newLocal(Type.getType("Z")); 
      pushConstant(0);
      this.mv.visitVarInsn(ISTORE, locals[i]);
    }


    this.mv.visitLabel(this.before);
  }

  @Override
  public void visitMaxs(final int maxStack, final int maxLocals) {

    this.mv.visitTryCatchBlock(this.before, this.handler, this.handler, null);
    this.mv.visitLabel(this.handler);

    this.mv.visitInsn(ATHROW);
    
    // values actually unimportant as we're using compute max
    this.mv.visitMaxs(maxStack, nextLocal);
  }

  @Override
  protected void onMethodExit(final int opcode) {
   //if ( opcode != ATHROW) {
    generateProbeReportCode();
  //  }
  }

  private void generateProbeReportCode() {
    
 
    pushConstant(this.classId);
    pushConstant(this.probeOffset);
    
    for ( int i : locals) {
      this.mv.visitVarInsn(ILOAD, i);
    }

    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CODE_COVERAGE_CALCULATOR_CLASS_NAME,
        "visitSingleLine",
        "(II" + String.format(String.format("%%0%dd", this.numberOfProbes), 0).replace("0","Z") +")V");
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    // get probe id - unique within parent class
    this.cv.registerLine(line);

    pushConstant(1);
    this.mv.visitVarInsn(ISTORE, locals[probeCount]);
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
