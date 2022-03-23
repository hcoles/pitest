package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanTrueReturnValsMutator.TRUE_RETURNS;

public class BooleanTrueReturnValsMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(TRUE_RETURNS);

    @Test
    public void mutatesReturnFalseToReturnTrue() {
        v.forCallableClass(BooleanReturn.class)
                .firstMutantShouldReturn("true");
    }

    @Test
    public void describesMutationsToPrimitiveBooleans() {
        v.forCallableClass(BooleanReturn.class)
                .firstMutantDescription()
                .contains("replaced boolean return with true")
                .contains("BooleanReturn::mutable");
    }

    @Test
    public void doesNotMutatePrimitiveIntReturns() {
        v.forCallableClass(IntegerReturn.class)
                .noMutantsCreated();
    }

    @Test
    public void mutatesBoxedFalseToTrue() {
        v.forCallableClass(BoxedFalse.class)
                .firstMutantShouldReturn(true);
    }

    @Test
    public void describesMutationsToBoxedBooleans() {
        v.forCallableClass(BoxedFalse.class)
                .firstMutantDescription()
                .contains("replaced Boolean return with True")
                .contains("BoxedFalse::call");
    }

    @Test
    public void doesNotMutateBoxedIntegerReturns() {
        v.forCallableClass(BoxedInteger.class)
                .noMutantsCreated();
    }

    private static class BooleanReturn implements Callable<String> {
        public boolean mutable() {
            return false;
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


    private static class BoxedFalse implements Callable<Boolean> {
        @Override
        public Boolean call() {
            return false;
        }
    }

    private static class BoxedInteger implements Callable<Integer> {
        @Override
        public Integer call() {
            return 42;
        }
    }


}
