package org.pitest.mutationtest.engine.gregor.mutators;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

public enum RemoveConditionalMutator implements MethodMutatorFactory {

  REMOVE_CONDITIONALS_MUTATOR;

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new RemoveConditionalMethodVisitor(this, context, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  public String getName() {
    return name();
  }

}

class RemoveConditionalMethodVisitor extends MethodVisitor {

  private static final String        DESCRIPTION = "removed conditional";
  private final Context              context;
  private final MethodMutatorFactory factory;

  public RemoveConditionalMethodVisitor(final MethodMutatorFactory factory,
      final Context context, final MethodVisitor delegateMethodVisitor) {
    super(Opcodes.ASM4, delegateMethodVisitor);
    this.context = context;
    this.factory = factory;
  }

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {

    if (canMutate(opcode)) {
      final MutationIdentifier newId = this.context.registerMutation(
          this.factory, DESCRIPTION);

      if (this.context.shouldMutate(newId)) {
         emptyStack(opcode);
      } else {
        this.mv.visitJumpInsn(opcode, label);
      }
    } else {
      this.mv.visitJumpInsn(opcode, label);
    }

  }

  private void emptyStack(int opcode) {
    switch(opcode) {
    case Opcodes.IF_ICMPNE:
    case Opcodes.IF_ICMPEQ:
    case Opcodes.IF_ACMPEQ:
    case Opcodes.IF_ACMPNE:
      super.visitInsn(Opcodes.POP2); 
      break;
    default:
      super.visitInsn(Opcodes.POP); 
    }

    
  }

  private boolean canMutate(final int opcode) {
    switch (opcode) {
    case Opcodes.IFEQ:
    case Opcodes.IFNE:
    case Opcodes.IFNONNULL:
    case Opcodes.IFNULL:
    case Opcodes.IF_ICMPNE:
    case Opcodes.IF_ICMPEQ:
    case Opcodes.IF_ACMPEQ:
    case Opcodes.IF_ACMPNE:
      return true;
    default:
      return false;
    }
  }

}
