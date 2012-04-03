package org.pitest.mutationtest.engine.gregor;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

class LineFilterMethodAdapter extends MethodAdapter {
  
  private final static String DISABLE_REASON = "AVOIDED_METHOD";

  private final Context              context;
  private final PremutationClassInfo classInfo;

  public LineFilterMethodAdapter(final Context context,
      final PremutationClassInfo classInfo,
      final MethodVisitor delegateMethodVisitor) {
    super(delegateMethodVisitor);
    this.context = context;
    this.classInfo = classInfo;
  }

  @Override
  public void visitLineNumber(final int line, final Label start) {
    if (this.classInfo.isLoggingLine(line)) {
      this.context.disableMutations(DISABLE_REASON);
    } else {
      this.context.enableMutatations(DISABLE_REASON);
    }
    this.mv.visitLineNumber(line, start);
  }

}
