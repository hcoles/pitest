package org.pitest.verifier.mutants;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.util.Unchecked;

import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on instantiating mutated versions of a class.
 * <p>
 * Classes must implement java.util.Function and provide a default
 * constructor.
 */
public class MutantVerifier<A, B> extends MutatorVerifier {

    private final GregorMutater engine;
    private final Class<? extends Function<A, B>> target;

    public MutantVerifier(GregorMutater engine, Class<? extends Function<A, B>> target, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        super(engine, target, filter, checkUnmutatedValues);
        this.engine = engine;
        this.target = target;
    }

    /**
     * Suppliers allow consumable inputs (eg streams) can be reused
     */
    public void firstMutantShouldReturn(Supplier<A> as, B expected) {
        A input = as.get();
        if (checkUnmutated()) {
            assertThat(runWithoutMutation(input))
                    .describedAs("Expected unmutated code to return different value to mutated code")
                    .isNotEqualTo(expected);
        }

        List<MutationDetails> mutations = findMutations();
        assertThat(mutateAndCall(input, getFirstMutant(mutations)))
                .isEqualTo(expected);
    }

    private B runWithoutMutation(A input) {
        return this.runInClassLoader(target.getClassLoader(), input);
    }

    private B mutateAndCall(A input, Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader, input);
    }

    private B runInClassLoader(ClassLoader loader, A input) {
        try {
            Class<?> forLoader = loader.loadClass(target.getName());

            Constructor c = forLoader.getDeclaredConstructor();
            c.setAccessible(true);
            Function<A, B> instance = (Function<A, B>) c.newInstance();
            return instance.apply(input);
        } catch (ReflectiveOperationException ex) {
            throw Unchecked.translateCheckedException(ex);
        }
    }

    protected Mutant getFirstMutant(final Collection<MutationDetails> actual) {
        assertThat(actual)
                .describedAs("Expecting at least one mutant to be generated")
                .isNotEmpty();
        final Mutant mutant = this.engine.getMutation(actual.iterator().next()
                .getId());
        verifyMutant(mutant);
        return mutant;
    }

}
