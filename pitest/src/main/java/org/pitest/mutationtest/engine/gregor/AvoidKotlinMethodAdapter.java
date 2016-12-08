package org.pitest.mutationtest.engine.gregor;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AvoidKotlinMethodAdapter extends MethodVisitor {
    private static final String DISABLE_REASON = "KOTLIN_INTRINSICS";
    private final MethodMutationContext context;

    AvoidKotlinMethodAdapter(MethodMutationContext context, MethodVisitor delegateMethodVisitor) {
        super(Opcodes.ASM5, delegateMethodVisitor);
        this.context = context;
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
                                final String name, final String desc, boolean itf) {

        if ("kotlin/jvm/internal/Intrinsics".equals(owner)) {
            this.context.disableMutations(DISABLE_REASON);
        }
        super.visitMethodInsn(opcode, owner, name, desc, itf);
    }
}
