package org.pitest.coverage.analysis;

import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;

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
public class SimpleBlockCoverageVisitor extends MethodVisitor {
  private final MethodVisitor      methodVisitor;
  private final int                classId;

  private final int                probeOffset;

  private final InstructionCounter counter;
  private final List<Block>        blocks;

  private int                      probeCount = 0;

  public SimpleBlockCoverageVisitor(List<Block> blocks,
      InstructionCounter counter, final int classId,
      final MethodVisitor writer, final int access, final String name,
      final String desc, final int probeOffset) {
    super(Opcodes.ASM6, writer);

    this.counter = counter;
    this.methodVisitor = writer;
    this.classId = classId;
    this.blocks = blocks;

    this.probeOffset = probeOffset;
  }

  @Override
  public void visitFrame(final int type, final int nLocal,
      final Object[] local, final int nStack, final Object[] stack) {
    insertProbeIfAppropriate();
    super.visitFrame(type, nLocal, local, nStack, stack);
  }

  @Override
  public void visitInsn(final int opcode) {
    insertProbeIfAppropriate();
    super.visitInsn(opcode);
  }

  @Override
  public void visitIntInsn(final int opcode, final int operand) {
    insertProbeIfAppropriate();
    super.visitIntInsn(opcode, operand);
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    insertProbeIfAppropriate();
    super.visitVarInsn(opcode, var);
  }

  @Override
  public void visitTypeInsn(final int opcode, final String type) {
    insertProbeIfAppropriate();
    super.visitTypeInsn(opcode, type);
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner,
      final String name, final String desc) {
    insertProbeIfAppropriate();
    super.visitFieldInsn(opcode, owner, name, desc);
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    insertProbeIfAppropriate();
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitInvokeDynamicInsn(final String name, final String desc,
      final Handle bsm, final Object... bsmArgs) {
    insertProbeIfAppropriate();
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
  }

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    insertProbeIfAppropriate();
    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitLabel(final Label label) {
    super.visitLabel(label);
    // note - probe goes after the label
    insertProbeIfAppropriate();
  }

  @Override
  public void visitLdcInsn(final Object cst) {
    insertProbeIfAppropriate();
    super.visitLdcInsn(cst);
  }

  @Override
  public void visitIincInsn(final int var, final int increment) {
    insertProbeIfAppropriate();
    super.visitIincInsn(var, increment);
  }

  @Override
  public void visitTableSwitchInsn(final int min, final int max,
      final Label dflt, final Label... labels) {
    insertProbeIfAppropriate();
    super.visitTableSwitchInsn(min, max, dflt, labels);
  }

  @Override
  public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
      final Label[] labels) {
    insertProbeIfAppropriate();
    super.visitLookupSwitchInsn(dflt, keys, labels);
  }

  @Override
  public void visitMultiANewArrayInsn(final String desc, final int dims) {
    insertProbeIfAppropriate();
    super.visitMultiANewArrayInsn(desc, dims);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    insertProbeIfAppropriate();
    super.visitLineNumber(line, start);
  }

  private void insertProbeIfAppropriate() {
    if (needsProbe(this.counter.currentInstructionCount())) {
      this.methodVisitor.visitLdcInsn(this.classId);
      this.methodVisitor.visitLdcInsn(this.probeCount + this.probeOffset);

      this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
          CodeCoverageStore.CLASS_NAME, "visitSingleProbe", "(II)V", false);

      this.probeCount++;
    }
  }

  private boolean needsProbe(int currentInstructionCount) {
    for (Block each : this.blocks) {
      if (each.firstInstructionIs(currentInstructionCount - 1)) {
        return true;
      }
    }
    return false;
  }

}
