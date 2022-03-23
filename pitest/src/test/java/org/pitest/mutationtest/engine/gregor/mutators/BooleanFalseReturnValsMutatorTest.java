package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanFalseReturnValsMutator.FALSE_RETURNS;

public class BooleanFalseReturnValsMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(FALSE_RETURNS);

    @Test
    public void mutatesReturnTrueToReturnFalse() {
        v.forCallableClass(BooleanReturn.class)
                .firstMutantShouldReturn("false");
    }

    @Test
    public void describesMutationsToPrimitiveBooleans() {
        v.forCallableClass(BooleanReturn.class)
                .firstMutantDescription()
                .contains("replaced boolean return with false")
                .contains("BooleanReturn::mutable");
    }

    @Test
    public void doesNotMutatePrimitiveIntReturns() {
        v.forClass(IntegerReturn.class)
                .noMutantsCreated();
    }

    @Test
    public void mutatesBoxedTrueToFalse() {
        v.forCallableClass(BoxedTrue.class)
                .firstMutantShouldReturn(false);
    }

    @Test
    public void describesMutationsToBoxedBooleans() {
        v.forCallableClass(BoxedTrue.class)
                .firstMutantDescription()
                .contains("replaced Boolean return with False")
                .contains("BoxedTrue::call");
    }

    @Test
    public void doesNotMutateBoxedIntegerReturns() {
        v.forClass(BoxedInteger.class)
                .noMutantsCreated();
    }

    private static class BooleanReturn implements Callable<String> {
        public boolean mutable() {
            return true;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

    private static class IntegerReturn implements Callable<String> {
        public int mutable() {
            return 42;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }


    private static class BoxedTrue implements Callable<Boolean> {
        @Override
        public Boolean call() {
            return Boolean.TRUE;
        }
    }

    private static class BoxedInteger implements Callable<Integer> {
        @Override
        public Integer call() {
            return 42;
        }
    }


}
