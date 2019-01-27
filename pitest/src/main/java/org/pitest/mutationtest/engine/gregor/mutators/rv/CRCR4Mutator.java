package org.pitest.mutationtest.engine.gregor.mutators.rv;


import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator that replaces an inline constant with -constant or with maxValue for minValue of int and long;
 */
public enum CRCR4Mutator implements MethodMutatorFactory {

    CRCR_4_MUTATOR;

    private final class CRCRVisitor1 extends AbstractCRCRVisitor {

        CRCRVisitor1(final MutationContext context,
                     final MethodVisitor delegateVisitor) {
            super(context, delegateVisitor, CRCR4Mutator.this);
        }

        void mutate(final Double constant) {
            final Double replacement = -constant;

            if ((! constant.equals(0D)) && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Float constant) {
            final Float replacement = -constant;

            if ((! constant.equals(0F)) && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Integer constant) {
            final Integer replacement = (constant == Integer.MIN_VALUE) ? Integer.MAX_VALUE : -constant;

            if ((! constant.equals(0)) && shouldMutate(constant, replacement)) {
                translateToByteCode(replacement);
            } else {
                translateToByteCode(constant);
            }
        }

        void mutate(final Long constant) {
            final Long replacement = (constant == Long.MIN_VALUE) ? Long.MAX_VALUE : -constant;

            if ((! constant.equals(0L)) && shouldMutate(constant, replacement)) {
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