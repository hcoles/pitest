package org.pitest.mutationtest.engine.gregor.mutators;

import org.junit.Test;
import org.pitest.verifier.mutants.MutatorVerifierStart;

import java.util.concurrent.Callable;

import static org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator.PRIMITIVE_RETURNS;

public class PrimitiveReturnMutatorTest {

    MutatorVerifierStart v = MutatorVerifierStart.forMutator(PRIMITIVE_RETURNS)
            .notCheckingUnMutatedValues();

    @Test
    public void doesNotMutateBooleans() {
        v.forClass(BooleanReturn.class)
                .noMutantsCreated();
    }

    @Test
    public void mutatesReturnTo0ForBytes() {
        v.forCallableClass(ByteReturn.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void describesMutationsToBytes() {
        v.forCallableClass(ByteReturn.class)
                .firstMutantDescription()
                .contains("replaced byte return with 0")
                .contains("ByteReturn::mutable");
    }

    @Test
    public void mutatesReturnToReturn0ForInts() {
        v.forCallableClass(IntReturn.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void describesMutationsToInts() {
        v.forCallableClass(IntReturn.class)
                .firstMutantDescription()
                .contains("replaced int return with 0")
                .contains("IntReturn::mutable");
    }

    @Test
    public void mutatesReturnToReturn0ForShorts() {
        v.forCallableClass(ShortReturn.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void describesMutationsToShorts() {
        v.forCallableClass(ShortReturn.class)
                .firstMutantDescription()
                .contains("replaced short return with 0")
                .contains("ShortReturn::mutable");
    }

    @Test
    public void mutatesReturnToReturn0ForChars() {
        v.forCallableClass(CharReturn.class)
                .firstMutantShouldReturn("" + (char) 0);
    }

    @Test
    public void describesMutationsToChars() {
        v.forCallableClass(CharReturn.class)
                .firstMutantDescription()
                .contains("replaced char return with 0")
                .contains("CharReturn::mutable");
    }

    @Test
    public void mutatesReturnToReturn0ForLongs() {
        v.forCallableClass(LongReturn.class)
                .firstMutantShouldReturn("0");
    }

    @Test
    public void describesMutationsToLongs() {
        v.forCallableClass(LongReturn.class)
                .firstMutantDescription()
                .contains("replaced long return with 0")
                .contains("LongReturn::mutable");
    }

    @Test
    public void mutatesReturnToReturn0ForFloats() {
        v.forCallableClass(FloatReturn.class)
                .firstMutantShouldReturn("0.0");
    }

    @Test
    public void describesMutationsToFloats() {
        v.forCallableClass(FloatReturn.class)
                .firstMutantDescription()
                .contains("replaced float return with 0.0f")
                .contains("FloatReturn::mutable");
    }

    @Test
    public void mutatesReturnToReturn0ForDoubles() {
        v.forCallableClass(DoubleReturn.class)
                .firstMutantShouldReturn("0.0");
    }

    @Test
    public void describesMutationsToDoubless() {
        v.forCallableClass(DoubleReturn.class)
                .firstMutantDescription()
                .contains("replaced double return with 0.0d")
                .contains("DoubleReturn::mutable");
    }

    @Test
    public void doesNotMutateObjectReturns() {
        v.forClass(BoxedIntReturn.class)
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

    private static class ByteReturn implements Callable<String> {
        public byte mutable() {
            return 101;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

    private static class IntReturn implements Callable<String> {
        public int mutable() {
            return 101;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }


    private static class ShortReturn implements Callable<String> {
        public short mutable() {
            return 1;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

    private static class CharReturn implements Callable<String> {
        public char mutable() {
            return 42;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

    private static class LongReturn implements Callable<String> {
        public long mutable() {
            return 10;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }


    private static class FloatReturn implements Callable<String> {
        public float mutable() {
            return Float.MAX_VALUE;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

    private static class DoubleReturn implements Callable<String> {
        public double mutable() {
            return Double.MAX_VALUE;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

    private static class BoxedIntReturn implements Callable<String> {
        public Integer mutable() {
            return null;
        }

        @Override
        public String call() {
            return "" + mutable();
        }
    }

}
