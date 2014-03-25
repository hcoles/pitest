package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import java.util.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

/**
 * Remove switch statements. We get an array of labels to jump to, plus a
 * default label. We change each label to the default label, thus removing
 * it
 */
public class RemoveSwitchMutator implements MethodMutatorFactory {
  //EXPERIMENTAL_REMOVE_SWITCH_MUTATOR;
  int key = 0;
  public RemoveSwitchMutator(int i) {
     key = i;
  }

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new RemoveSwitchMethodVisitor(context, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  public String getName() {
    return toString();
  }

  @Override
  public String toString() {
    return "EXPERIMENTAL_REMOVE_SWITCH_MUTATOR_" + key;
  }

  private final class RemoveSwitchMethodVisitor extends MethodVisitor {

    private final Context context;

    public RemoveSwitchMethodVisitor(final Context context,
        final MethodVisitor methodVisitor) {
      super(Opcodes.ASM4, methodVisitor);
      this.context = context;
    }

    @Override
    public void visitTableSwitchInsn(final int i, final int i1,
        final Label defaultLabel, final Label... labels) {
      if (labels.length > key && shouldMutate()) {
        Label[] newLabels = labels.clone();
        newLabels[key] = defaultLabel;
        super.visitTableSwitchInsn(i, i1, defaultLabel, newLabels);
      } else {
        super.visitTableSwitchInsn(i, i1, defaultLabel, labels);
      }
    }

    @Override
    public void visitLookupSwitchInsn(final Label defaultLabel, final int[] ints, final Label[] labels) {
      if (labels.length > key && shouldMutate()) {
        Label[] newLabels = labels.clone();
        newLabels[key] = defaultLabel;
        super.visitLookupSwitchInsn(defaultLabel, ints, newLabels);
      } else {
        super.visitLookupSwitchInsn(defaultLabel, ints, labels);
      }
    }

    private boolean shouldMutate() {
      final MutationIdentifier mutationId = this.context.registerMutation(
          RemoveSwitchMutator.this, "RemoveSwitch " + key + " mutation");
      return this.context.shouldMutate(mutationId);
    }

  }

}
