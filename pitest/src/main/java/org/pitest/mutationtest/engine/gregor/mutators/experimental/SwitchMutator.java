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
        public void visitTableSwitchInsn(int i, int i1, Label defaultLabel, Label[] labels) {
            Label newDefault = firstDifferentLabel(labels, defaultLabel);
            if (newDefault != null) {
              Label[] newLabels = swapLabels(labels, defaultLabel, newDefault);
              super.visitTableSwitchInsn(i, i1, newDefault, newLabels);
              this.context.registerMutation(SwitchMutator.this, "Switch mutation");
            } else {
              super.visitTableSwitchInsn(i, i1, defaultLabel, labels);
            }
        }

        @Override
        public void visitLookupSwitchInsn(Label defaultLabel, int[] ints, Label[] labels) {
            Label newDefault = firstDifferentLabel(labels, defaultLabel);
            if (newDefault != null) {
              Label[] newLabels = swapLabels(labels, defaultLabel, newDefault);
              super.visitLookupSwitchInsn(newDefault, ints, newLabels);
              this.context.registerMutation(SwitchMutator.this, "Switch mutation");
            } else {
              super.visitLookupSwitchInsn(defaultLabel, ints, labels);
            }
        }

        private Label[] swapLabels(Label[] labels, Label defaultLabel, Label newDefault) {
            Label[] swapped = new Label[labels.length];
            for (int i = 0 ; i < labels.length ; i++) {
                Label candidate = labels[i];
                if (candidate == defaultLabel) {
                    swapped[i] = newDefault;
                } else {
                    swapped[i] = defaultLabel;
                }
            }
            return swapped;
        }

        private Label firstDifferentLabel(Label[] labels, Label label) {
            for (Label candidate : labels) {
                if (candidate != label) {
                    return candidate;
                }
            }
            return null;
        }
    }


}
