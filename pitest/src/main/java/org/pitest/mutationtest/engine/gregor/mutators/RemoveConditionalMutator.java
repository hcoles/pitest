package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public class RemoveConditionalMutator implements MethodMutatorFactory {

  // REMOVE_CONDITIONALS_MUTATOR;

  // EQUAL : Mutate only equality operators
  // ORDER : Mutate only Ordering operators
  public enum Choice {
    EQUAL("equality"), ORDER("comparison");
    private String desc;

    Choice(String desc) {
      this.desc = desc;
    }

    String description() {
      return this.desc;
    }
  }

  private final Choice  kind;
  private final boolean replaceWith; // Default is true

  public RemoveConditionalMutator(final Choice c, final boolean rc) {
    this.kind = c;
    this.replaceWith = rc;
  }

  public static Iterable<MethodMutatorFactory> makeMutators() {
    final List<MethodMutatorFactory> variations = new ArrayList<>();
    final Choice[] allChoices = { Choice.EQUAL, Choice.ORDER };
    final boolean[] arrWith = { true, false };
    for (final Choice c : allChoices) {
      for (final boolean b : arrWith) {
        variations.add(new RemoveConditionalMutator(c, b));
      }
    }
    return variations;
  }

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new RemoveConditionalMethodVisitor(this, context, methodVisitor,
        "removed conditional - replaced " + this.kind.description()
            + " check with " + this.replaceWith);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName() + "_" + this.kind + "_"
        + (this.replaceWith ? "IF" : "ELSE");
  }

  @Override
  public String getName() {
    return "REMOVE_CONDITIONALS_" + this.kind + "_"
        + (this.replaceWith ? "IF" : "ELSE") + "_MUTATOR";
  }

  private final class RemoveConditionalMethodVisitor extends MethodVisitor {

    private final String               description;
    private final MutationContext      context;
    private final MethodMutatorFactory factory;

    RemoveConditionalMethodVisitor(final MethodMutatorFactory factory,
        final MutationContext context,
        final MethodVisitor delegateMethodVisitor, String description) {
      super(ASMVersion.ASM_VERSION, delegateMethodVisitor);
      this.context = context;
      this.factory = factory;
      this.description = description;
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {

      if (canMutate(opcode)) {
        final MutationIdentifier newId = this.context.registerMutation(
            this.factory, this.description);

        if (this.context.shouldMutate(newId)) {
          emptyStack(opcode);
          if (!RemoveConditionalMutator.this.replaceWith) {
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
        return (RemoveConditionalMutator.this.kind == Choice.ORDER);
      case Opcodes.IFEQ:
      case Opcodes.IFNE:
      case Opcodes.IFNONNULL:
      case Opcodes.IFNULL:
      case Opcodes.IF_ICMPNE:
      case Opcodes.IF_ICMPEQ:
      case Opcodes.IF_ACMPEQ:
      case Opcodes.IF_ACMPNE:
        return (RemoveConditionalMutator.this.kind == Choice.EQUAL);
      default:
        return false;
      }
    }

  }
}
