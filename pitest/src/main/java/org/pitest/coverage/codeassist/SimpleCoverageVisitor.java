package org.pitest.coverage.codeassist;


import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import sun.pitest.CodeCoverageStore;

/**
 * Instruments via a method call at each line.
 * 
 * This simplistic approach is generally slow, but does not require finally
 * blocks that are difficult to generate correctly for constructors.
 * 
 * This simple approach should however provide better performance for single
 * line methods.
 */
public class SimpleCoverageVisitor extends MethodVisitor {
  private final MethodVisitor methodVisitor;
  private final int           classId;
  private final LineTracker   lineTracker;
  private final int           probeOffset;

  private int                 probeCount = 0;

  public SimpleCoverageVisitor(final LineTracker lineTracker,
      final int classId, final MethodVisitor writer, final int access,
      final String name, final String desc, final int probeOffset) {
    super(Opcodes.ASM5, writer);

    this.methodVisitor = writer;
    this.classId = classId;
    this.lineTracker = lineTracker;
    this.probeOffset = probeOffset;
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {

    this.lineTracker.registerLine(line);
    this.methodVisitor.visitLdcInsn(this.classId);
    this.methodVisitor.visitLdcInsn(this.probeCount + this.probeOffset);

    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CLASS_NAME, "visitSingleProbe", "(II)V", false);

    this.probeCount++;

    super.visitLineNumber(line, start);
  }

}
