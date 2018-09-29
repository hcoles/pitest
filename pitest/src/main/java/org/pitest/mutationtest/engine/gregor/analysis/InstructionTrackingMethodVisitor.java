package org.pitest.mutationtest.engine.gregor.analysis;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;

public class InstructionTrackingMethodVisitor extends MethodVisitor {

  private final InstructionCounter count;

  public InstructionTrackingMethodVisitor(final MethodVisitor mv,
      final InstructionCounter count) {
    super(ASMVersion.ASM_VERSION, mv);
    this.count = count;
  }

  @Override
  public void visitFrame(final int type, final int nLocal,
      final Object[] local, final int nStack, final Object[] stack) {
    this.count.increment();
    super.visitFrame(type, nLocal, local, nStack, stack);
  }

  @Override
  public void visitInsn(final int opcode) {
    this.count.increment();
    super.visitInsn(opcode);
  }

  @Override
  public void visitIntInsn(final int opcode, final int operand) {
    this.count.increment();
    super.visitIntInsn(opcode, operand);
  }

  @Override
  public void visitVarInsn(final int opcode, final int var) {
    this.count.increment();
    super.visitVarInsn(opcode, var);
  }

  @Override
  public void visitTypeInsn(final int opcode, final String type) {
    this.count.increment();
    super.visitTypeInsn(opcode, type);
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner,
      final String name, final String desc) {
    this.count.increment();
    super.visitFieldInsn(opcode, owner, name, desc);
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    this.count.increment();
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitInvokeDynamicInsn(final String name, final String desc,
      final Handle bsm, final Object... bsmArgs) {
    this.count.increment();
    super.visitInvokeDynamicInsn(name, desc, bsm, bsmArgs);
  }

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    this.count.increment();
    super.visitJumpInsn(opcode, label);
  }

  @Override
  public void visitLabel(final Label label) {
    this.count.increment();
    super.visitLabel(label);
  }

  @Override
  public void visitLdcInsn(final Object cst) {
    this.count.increment();
    super.visitLdcInsn(cst);
  }

  @Override
  public void visitIincInsn(final int var, final int increment) {
    this.count.increment();
    super.visitIincInsn(var, increment);
  }

  @Override
  public void visitTableSwitchInsn(final int min, final int max,
      final Label dflt, final Label... labels) {
    this.count.increment();
    super.visitTableSwitchInsn(min, max, dflt, labels);
  }

  @Override
  public void visitLookupSwitchInsn(final Label dflt, final int[] keys,
      final Label[] labels) {
    this.count.increment();
    super.visitLookupSwitchInsn(dflt, keys, labels);
  }

  @Override
  public void visitMultiANewArrayInsn(final String desc, final int dims) {
    this.count.increment();
    super.visitMultiANewArrayInsn(desc, dims);
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    this.count.increment();
    super.visitLineNumber(line, start);
  }

}
