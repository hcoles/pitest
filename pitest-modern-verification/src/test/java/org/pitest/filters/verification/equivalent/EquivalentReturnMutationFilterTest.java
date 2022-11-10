package org.pitest.filters.verification.equivalent;

import org.junit.Test;
import org.pitest.mutationtest.build.intercept.equivalent.EquivalentReturnMutationFilter;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import static org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator.EMPTY_RETURNS;


public class EquivalentReturnMutationFilterTest {

    InterceptorVerifier v = VerifierStart.forInterceptorFactory(new EquivalentReturnMutationFilter());

    @Test
    public void filtersWhenMapOfHasNoArgs() {
        v.usingMutator(EMPTY_RETURNS)
                .forClass(HasMapOf.class)
                .forAnyCode()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersWhenSetOfHasNoArgs() {
        v.usingMutator(EMPTY_RETURNS)
                .forClass(HasSetOf.class)
                .forAnyCode()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void filtersWhenListOfHasNoArgs() {
        v.usingMutator(EMPTY_RETURNS)
                .forClass(HasListOf.class)
                .forAnyCode()
                .allMutantsAreFiltered()
                .verify();
    }

    @Test
    public void doesNotFilterNonEmptyMapOf() {
        v.usingMutator(EMPTY_RETURNS)
                .forClass(NonEmptyMapOf.class)
                .forAnyCode()
                .noMutantsAreFiltered()
                .verify();
    }

}
