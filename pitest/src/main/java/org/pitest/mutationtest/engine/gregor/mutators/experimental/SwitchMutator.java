package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutate switch statements. We get an array of labels to jump to, plus a
 * default label. We find the first label that differs from the default and use
 * this in place of the default label. All other labels are replaced by the
 * default.
 */
public class SwitchMutator implements MethodMutatorFactory {

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new SwitchMethodVisitor(context, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return "EXPERIMENTAL_SWITCH_MUTATOR";
  }

  private final class SwitchMethodVisitor extends MethodVisitor {

    private final MutationContext context;

    SwitchMethodVisitor(final MutationContext context,
        final MethodVisitor methodVisitor) {
      super(ASMVersion.ASM_VERSION, methodVisitor);
      this.context = context;
    }

    @Override
    public void visitTableSwitchInsn(final int i, final int i1,
        final Label defaultLabel, final Label... labels) {
      final Label newDefault = firstDifferentLabel(labels, defaultLabel);
      if ((newDefault != null) && shouldMutate()) {
        final Label[] newLabels = swapLabels(labels, defaultLabel, newDefault);
        super.visitTableSwitchInsn(i, i1, newDefault, newLabels);
      } else {
        super.visitTableSwitchInsn(i, i1, defaultLabel, labels);
      }
    }

    @Override
    public void visitLookupSwitchInsn(final Label defaultLabel,
        final int[] ints, final Label[] labels) {
      final Label newDefault = firstDifferentLabel(labels, defaultLabel);
      if ((newDefault != null) && shouldMutate()) {
        final Label[] newLabels = swapLabels(labels, defaultLabel, newDefault);
        super.visitLookupSwitchInsn(newDefault, ints, newLabels);
      } else {
        super.visitLookupSwitchInsn(defaultLabel, ints, labels);
      }
    }

    private Label[] swapLabels(final Label[] labels, final Label defaultLabel,
        final Label newDefault) {
      final Label[] swapped = new Label[labels.length];
      for (int i = 0; i < labels.length; i++) {
        final Label candidate = labels[i];
        if (candidate == defaultLabel) {
          swapped[i] = newDefault;
        } else {
          swapped[i] = defaultLabel;
        }
      }
      return swapped;
    }

    private Label firstDifferentLabel(final Label[] labels, final Label label) {
      for (final Label candidate : labels) {
        if (candidate != label) {
          return candidate;
        }
      }
      return null;
    }

    private boolean shouldMutate() {
      final MutationIdentifier mutationId = this.context.registerMutation(
          SwitchMutator.this, "Switch mutation");
      return this.context.shouldMutate(mutationId);
    }

  }

}
