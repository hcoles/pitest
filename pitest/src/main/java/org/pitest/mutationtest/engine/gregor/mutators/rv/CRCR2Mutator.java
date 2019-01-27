package org.pitest.mutationtest.engine.gregor.mutators.rv;


import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator that replaces an inline constant with 0;
 */
public enum CRCR2Mutator implements MethodMutatorFactory {

    CRCR_2_MUTATOR;

    private final class CRCRVisitor1 extends AbstractCRCRVisitor {

        CRCRVisitor1(final MutationContext context,
                     final MethodVisitor delegateVisitor) {
            super(context, delegateVisitor, CRCR2Mutator.this);
        }

        void mutate(final Double constant) {
            final Double replacement = 0D;

            if (constant != 0D && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Float constant) {
            final Float replacement = 0F;

            if (constant != 0F && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Integer constant) {
            final Integer replacement = 0;

            if (constant != 0 && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Long constant) {

            final Long replacement = 0L;

            if (constant != 0L && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }

        }
    }

    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new CRCRVisitor1(context, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }

}