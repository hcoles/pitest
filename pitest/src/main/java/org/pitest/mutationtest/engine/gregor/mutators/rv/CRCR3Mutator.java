package org.pitest.mutationtest.engine.gregor.mutators.rv;


import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator that replaces an inline constant with -1;
 */
public enum CRCR3Mutator implements MethodMutatorFactory {

    CRCR_3_MUTATOR;

    private final class CRCRVisitor1 extends AbstractCRCRVisitor {

        CRCRVisitor1(final MutationContext context,
                     final MethodVisitor delegateVisitor) {
            super(context, delegateVisitor, CRCR3Mutator.this);
        }

        void mutate(final Double constant) {
            final Double replacement = -1D;

            if ((! constant.equals(-1D)) && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Float constant) {
            final Float replacement = -1F;

            if ((! constant.equals(-1F)) && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Integer constant) {
            final Integer replacement = -1;

            if ((! constant.equals(-1)) && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Long constant) {

            final Long replacement = -1L;

            if ((! constant.equals(-1L)) && shouldMutate(constant, replacement)) {
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