package org.pitest.mutationtest.build.intercept.javafeatures;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.util.function.Predicate;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator.INCREMENTS;
import static org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator.VOID_METHOD_CALLS;

public class InlinedFinallyBlockFilterTest {

    InlinedFinallyBlockFilter testee = new InlinedFinallyBlockFilter();

    FilterTester verifier = new FilterTester("trywithresources/{0}_{1}", this.testee,
            // omit aspectJ. Filter doesn't work correctly with it, but slack is picked up by the
            // try with resources filter
            asList("javac", "javac11", "ecj"),
            VOID_METHOD_CALLS, INCREMENTS);

    @Test
    public void shouldDeclareTypeAsFilter() {
        assertEquals(InterceptorType.FILTER, this.testee.type());
    }

    @Test
    public void doesNotFilterWhenNoFinallyBlock() {
        verifier.assertFiltersNMutationFromClass(0, TryCatchNoFinally.class);
    }

    @Test
    public void combinesMutationsInSimpleFinallyBlocks() {
        verifier.assertFiltersNMutationFromClass(1, HasFinallyBlock.class);
        verifier.assertCombinedMutantExists(forMutator(INCREMENTS), HasFinallyBlock.class);
    }

    @Test
    public void combinesMutationsInFinallyBlocksWithExceptionHandlers() {
        verifier.assertFiltersNMutationFromClass(2, HasFinallyBlockAndExceptionHandler.class);
        verifier.assertCombinedMutantExists(forMutator(INCREMENTS), HasFinallyBlockAndExceptionHandler.class);
    }

    @Test
    public void combinesSimilarMutationsInFinallyBlocksWhenOnDifferentLines() {
        verifier.assertFiltersNMutationFromClass(2, HasSimilarMutationsFinallyBlock.class);
        verifier.assertCombinedMutantExists(forMutator(INCREMENTS), HasSimilarMutationsFinallyBlock.class);
    }

    @Test
    public void filtersMutantsInTryCatchFinallySamples() {
        verifier.assertFiltersNMutationFromSample(3, "TryCatchFinallyExample");
    }

    @Test
    public void filtersMutantsInTryFinallySamples() {
        verifier.assertFiltersNMutationFromSample(2, "TryFinallyExample");
    }

    private Predicate<MutationDetails> forMutator(MethodMutatorFactory mutator) {
        return m -> m.getMutator().equals(mutator.getGloballyUniqueId());
    }


}

class TryCatchNoFinally {
    public void foo(int i) {
        try {
            System.out.println("foo");
        } catch(Exception ex) {
            i++;
        }
    }
}

class HasFinallyBlock {
    public void foo(int i) {
        try {
            System.out.println("foo");
        } finally {
            i++;
        }
    }
}

class HasSimilarMutationsFinallyBlock {
    public void foo(int i) {
        try {
            System.out.println("foo");
        } finally {
            i++;
            i++;
        }
    }
}

class HasFinallyBlockAndExceptionHandler {
    public void foo(int i) {
        try {
            System.out.println("foo");
        } catch (final Exception x) {
            System.out.println("bar");
        } finally {
            i++;
        }
    }
}
