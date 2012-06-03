package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

/*
  We want to mutate switch statements. We get an array of labels to jump to, plus a default label.
  If the array is empty then we can't mutate. Otherwise we should reorder the labels so that
  each entry in the new array differs from the equivalent in the original array. Similarly for
  the default label.
 */
public class SwitchMutator implements MethodMutatorFactory {

    public MethodVisitor create(Context context, MethodInfo methodInfo, MethodVisitor methodVisitor) {
        return new SwitchMethodVisitor(context, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return "EXPERIMENTAL_SWITCH_MUTATOR";
    }

    private final class SwitchMethodVisitor extends MethodAdapter {

        private final Context       context;

        public SwitchMethodVisitor(Context context, MethodVisitor methodVisitor) {
            super(methodVisitor);
            this.context = context;
        }

        @Override
        public void visitTableSwitchInsn(int i, int i1, Label label, Label[] labels) {
            super.visitTableSwitchInsn(i, i1, label, labels);
        }

        @Override
        public void visitLookupSwitchInsn(Label defaultLabel, int[] ints, Label[] labels) {
            this.context.registerMutation(SwitchMutator.this, "Switch mutation");
            Label newDefault = labels[0];
            Label[] newLabels = new Label[]{defaultLabel};
            int[] newInts = new int[]{ints[0]};
            super.visitLookupSwitchInsn(newDefault, newInts, newLabels);
        }
    }


}
