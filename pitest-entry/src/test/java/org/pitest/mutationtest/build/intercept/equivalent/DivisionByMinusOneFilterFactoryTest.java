package org.pitest.mutationtest.build.intercept.equivalent;

import org.junit.Test;
import org.pitest.bytecode.analysis.OpcodeMatchers;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.FactoryVerifier;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

public class DivisionByMinusOneFilterFactoryTest {

    DivisionByMinusOneFilterFactory underTest = new DivisionByMinusOneFilterFactory();

    InterceptorVerifier v = VerifierStart.forInterceptorFactory(underTest)
            .usingMutator(MathMutator.MATH);

    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnChain();
    }

    @Test
    public void doesNotFilterNonEquivalentMutants() {
        v.forClass(NonEquivalentMultiplication.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterMutantsFromOtherMutators() {
        VerifierStart.forInterceptorFactory(underTest)
                .usingMutator(new NullMutateEverything())
                .forClass(EquivalentMultiplicationByMinus1.class)
                .forAnyCode()
                .mutantsAreGenerated()
                .noMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToIMulNegative1() {
        v.forClass(EquivalentBoxedMultiplicationByMinus1.class)
                .forCodeMatching(OpcodeMatchers.IMUL.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToBoxedIMulNegative1() {
        v.forClass(EquivalentBoxedMultiplicationByMinus1.class)
                .forCodeMatching(OpcodeMatchers.IMUL.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToLMulNegative1() {
        v.forClass(EquivalentLongMultiplicationByMinus1.class)
                .forCodeMatching(OpcodeMatchers.LMUL.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToFMulNegative1() {
        v.forClass(EquivalentFloatMultiplicationByMinus1.class)
                .forCodeMatching(OpcodeMatchers.FMUL.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersMutationsToDMulNegative1() {
        v.forClass(EquivalentDoubleMultiplicationByMinus1.class)
                .forCodeMatching(OpcodeMatchers.DMUL.asPredicate())
                .mutantsAreGenerated()
                .allMutantsAreFiltered()
                .verify();
    }

}


class NonEquivalentMultiplication {
    public int mutateMe(int a, int b) {
        return (a + b) * -2;
    }
}

class EquivalentMultiplicationByMinus1 {
    public int mutateMe(int a, int b) {
        return (a + b) * -1;
    }
}

class EquivalentBoxedMultiplicationByMinus1 {
    public Integer mutateMe(Integer a, Integer b) {
        return (a + b) * -1;
    }
}

class EquivalentLongMultiplicationByMinus1 {
    public long mutateMe(long a, long b) {
        return (a + b) * -1;
    }
}

class EquivalentFloatMultiplicationByMinus1 {
    public float mutateMe(float a, float b) {
        return (a + b) * -1;
    }
}

class EquivalentDoubleMultiplicationByMinus1 {
    public double mutateMe(double a, double b) {
        return (a + b) * -1;
    }
}