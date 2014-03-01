package org.pitest.mutationtest.engine.gregor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class LineFilterMethodAdapter extends MethodVisitor {

  private final static String        DISABLE_REASON = "AVOIDED_LINE";

  private final Context              context;
  private final PremutationClassInfo classInfo;

  public LineFilterMethodAdapter(final Context context,
      final PremutationClassInfo classInfo,
      final MethodVisitor delegateMethodVisitor) {
    super(Opcodes.ASM4, delegateMethodVisitor);
    this.context = context;
    this.classInfo = classInfo;
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    if (this.classInfo.isLineToAvoid(line)) {
      this.context.disableMutations(DISABLE_REASON);
    } else {
      this.context.enableMutatations(DISABLE_REASON);
    }
    this.mv.visitLineNumber(line, start);
  }

}
