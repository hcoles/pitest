package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.List;
import java.util.ArrayList;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public class RemoveConditionalMutator implements MethodMutatorFactory {

  //REMOVE_CONDITIONALS_MUTATOR;

  // EQUAL : Mutate only equality operators
  // ORDER : Mutate only Ordering operators
  public enum Choice {EQUAL, ORDER}

  private final Choice kind;
  private final boolean replaceWith; // Default is true

  public RemoveConditionalMutator(final Choice c, final boolean rc) {
    kind = c;
    replaceWith = rc;
  }

  public static Iterable<MethodMutatorFactory> makeMutators() {
    List<MethodMutatorFactory> variations = new ArrayList<MethodMutatorFactory>();
    Choice [] allChoices = {Choice.EQUAL, Choice.ORDER};
    boolean [] arrWith = {true, false};
    for (Choice c : allChoices) {
      for (boolean b : arrWith) {
        variations.add(new RemoveConditionalMutator(c, b));
      }
    }
    return variations;
  }

  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new RemoveConditionalMethodVisitor(this, context, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName() + "_" + kind + "_" + (replaceWith ? "IF" : "ELSE");
  }

  public String getName() {
    return "REMOVE_CONDITIONALS_" + kind + "_" + (replaceWith ? "IF" : "ELSE") + "_MUTATOR";
  }

  private final class RemoveConditionalMethodVisitor extends MethodVisitor {

    private static final String        DESCRIPTION = "removed conditional";
    private final MutationContext              context;
    private final MethodMutatorFactory factory;

    public RemoveConditionalMethodVisitor(final MethodMutatorFactory factory,
        final MutationContext context, final MethodVisitor delegateMethodVisitor) {
      super(Opcodes.ASM5, delegateMethodVisitor);
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
          if (!replaceWith) {
            super.visitJumpInsn(Opcodes.GOTO, label);
          }
        } else {
          this.mv.visitJumpInsn(opcode, label);
        }
      } else {
        this.mv.visitJumpInsn(opcode, label);
      }

    }

    private void emptyStack(final int opcode) {
      switch (opcode) {
        // EQUAL
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ACMPEQ:
        case Opcodes.IF_ACMPNE:
        // ORDER
        case Opcodes.IF_ICMPGE:
        case Opcodes.IF_ICMPGT:
        case Opcodes.IF_ICMPLE:
        case Opcodes.IF_ICMPLT:
          super.visitInsn(Opcodes.POP2);
          break;
        default:
          super.visitInsn(Opcodes.POP);
      }

    }

    private boolean canMutate(final int opcode) {
      switch (opcode) {
        case Opcodes.IFLE:
        case Opcodes.IFGE:
        case Opcodes.IFGT:
        case Opcodes.IFLT:
        case Opcodes.IF_ICMPGE:
        case Opcodes.IF_ICMPGT:
        case Opcodes.IF_ICMPLE:
        case Opcodes.IF_ICMPLT:
          return (kind == Choice.ORDER);
        case Opcodes.IFEQ:
        case Opcodes.IFNE:
        case Opcodes.IFNONNULL:
        case Opcodes.IFNULL:
        case Opcodes.IF_ICMPNE:
        case Opcodes.IF_ICMPEQ:
        case Opcodes.IF_ACMPEQ:
        case Opcodes.IF_ACMPNE:
          return (kind == Choice.EQUAL);
        default:
          return false;
      }
    }

  }
}
