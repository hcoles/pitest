package org.pitest.coverage.analysis;

import java.util.List;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;

abstract class AbstractCoverageStrategy extends AdviceAdapter {

  protected final MethodVisitor    methodVisitor;
  protected final int              classId;
  protected final int              probeOffset;
  protected final List<Block>      blocks;

  private final InstructionCounter counter;

  /**
   * label to mark start of try finally block that is added to each method
   */
  private final Label              before     = new Label();

  /**
   * label to mark handler block of try finally
   */
  private final Label              handler    = new Label();

  protected int                    probeCount = 0;

  AbstractCoverageStrategy(List<Block> blocks, InstructionCounter counter,
      final int classId, final MethodVisitor writer, final int access,
      final String name, final String desc, final int probeOffset) {
    super(Opcodes.ASM6, writer, access, name, desc);

    this.methodVisitor = writer;
    this.classId = classId;
    this.counter = counter;
    this.blocks = blocks;
    this.probeOffset = probeOffset;
  }

  abstract void prepare();

  abstract void generateProbeReportCode();

  abstract void insertProbe();

  @Override
  public void visitCode() {
    super.visitCode();

    prepare();

    this.mv.visitLabel(this.before);
  }

  @Override
  public void visitMaxs(final int maxStack, final int maxLocals) {

    this.mv.visitTryCatchBlock(this.before, this.handler, this.handler, null);
    this.mv.visitLabel(this.handler);

    generateProbeReportCode();

    this.mv.visitInsn(ATHROW);

    // values actually unimportant as we're using compute max
    this.mv.visitMaxs(maxStack, this.nextLocal);
  }

  @Override
  protected void onMethodExit(final int opcode) {
    // generated catch block will handle any throws ending block
    if (opcode != ATHROW) {
      generateProbeReportCode();
    }
  }

  protected void pushConstant(final int value) {
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
      insertProbe();
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