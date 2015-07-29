package org.pitest.coverage.analysis;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;

import sun.pitest.CodeCoverageStore;

/**
 * Uses local variables as block probes within methods.
 *
 * Inserts a finally block the method, posting the probes hits to the code
 * coverage store via specialised methods for each number of probes.
 *
 * The range of methods this approach can be applied to is limited by the
 * maximum airty of the overloaded methods on the coverage store.
 */
class LocalVariableCoverageMethodVisitor extends AbstractCoverageStrategy {

  private int[] locals;

  LocalVariableCoverageMethodVisitor(final List<Block> blocks,
      final InstructionCounter counter, final int classId,
      final MethodVisitor writer, final int access, final String name,
      final String desc, final int probeOffset) {
    super(blocks, counter, classId, writer, access, name, desc, probeOffset);
  }

  @Override
  void prepare() {
    this.locals = new int[this.blocks.size()];
    for (int i = 0; i != this.blocks.size(); i++) {
      this.locals[i] = newLocal(Type.getType("Z"));
      pushConstant(0);
      this.mv.visitVarInsn(ISTORE, this.locals[i]);
    }
  }

  @Override
  void insertProbe() {
    pushConstant(1);
    this.mv.visitVarInsn(ISTORE, this.locals[this.probeCount]);
  }

  @Override
  protected void generateProbeReportCode() {

    pushConstant(this.classId);
    pushConstant(this.probeOffset);

    for (final int i : this.locals) {
      this.mv.visitVarInsn(ILOAD, i);
    }

    this.methodVisitor.visitMethodInsn(Opcodes.INVOKESTATIC,
        CodeCoverageStore.CLASS_NAME, CodeCoverageStore.PROBE_METHOD_NAME,
        "(II"
            + String.format(String.format("%%0%dd", this.blocks.size()), 0)
            .replace("0", "Z") + ")V", false);
  }

}
