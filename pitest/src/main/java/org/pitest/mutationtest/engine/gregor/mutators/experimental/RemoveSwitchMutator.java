package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Remove switch statements. We get an array of labels to jump to, plus a
 * default label. We change each label to the default label, thus removing it
 */
public class RemoveSwitchMutator implements MethodMutatorFactory {

  private static final String REMOVE_SWITCH_MUTATOR_NAME = "EXPERIMENTAL_REMOVE_SWITCH_MUTATOR_";
  private static final int GENERATE_FROM_INCLUDING = 0;
  private static final int GENERATE_UPTO_EXCLUDING = 100;

  private final int key;

  RemoveSwitchMutator(final int i) {
    this.key = i;
  }

  static List<MethodMutatorFactory> makeMutators() {
    final List<MethodMutatorFactory> variations = new ArrayList<>();
    for (int i = GENERATE_FROM_INCLUDING; i != GENERATE_UPTO_EXCLUDING; i++) {
      variations.add(new RemoveSwitchMutator(i));
    }
    return variations;
  }

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new RemoveSwitchMethodVisitor(context, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName() + "_" + this.key;
  }

  @Override
  public String getName() {
    return REMOVE_SWITCH_MUTATOR_NAME + "[" + GENERATE_FROM_INCLUDING + "-" + (GENERATE_UPTO_EXCLUDING - 1) + "]";
  }

  @Override
  public String toString() {
    return REMOVE_SWITCH_MUTATOR_NAME + this.key;
  }

  private final class RemoveSwitchMethodVisitor extends MethodVisitor {

    private final MutationContext context;

    RemoveSwitchMethodVisitor(final MutationContext context,
        final MethodVisitor methodVisitor) {
      super(ASMVersion.ASM_VERSION, methodVisitor);
      this.context = context;
    }

    @Override
    public void visitTableSwitchInsn(final int min, final int max,
        final Label defaultLabel, final Label... labels) {
      if ((labels.length > RemoveSwitchMutator.this.key) && labels[key] != defaultLabel && shouldMutate(value(min,max))) {
        final Label[] newLabels = labels.clone();
        newLabels[RemoveSwitchMutator.this.key] = defaultLabel;
        super.visitTableSwitchInsn(min, max, defaultLabel, newLabels);
      } else {
        super.visitTableSwitchInsn(min, max, defaultLabel, labels);
      }
    }

    @Override
    public void visitLookupSwitchInsn(final Label defaultLabel,
        final int[] ints, final Label[] labels) {
      if ((labels.length > RemoveSwitchMutator.this.key) && labels[key] != defaultLabel && shouldMutate(ints[key])) {
        final Label[] newLabels = labels.clone();
        newLabels[RemoveSwitchMutator.this.key] = defaultLabel;
        super.visitLookupSwitchInsn(defaultLabel, ints, newLabels);
      } else {
        super.visitLookupSwitchInsn(defaultLabel, ints, labels);
      }
    }

    private boolean shouldMutate(int value) {
      final MutationIdentifier mutationId = this.context.registerMutation(
          RemoveSwitchMutator.this, "RemoveSwitch "
              + RemoveSwitchMutator.this.key + " (case value " + value + ")");
      return this.context.shouldMutate(mutationId);
    }

    private int value(int min, int max) {
      return min + key;
    }

  }

}
