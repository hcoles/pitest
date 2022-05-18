package org.pitest.verifier.mutants;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.util.Unchecked;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.LongFunction;
import java.util.function.LongSupplier;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on instantiating mutated versions of a class.
 * <p>
 * Classes must implement java.util.IntFunction and provide a default
 * constructor.
 */
public class LongMutantVerifier<B> extends MutatorVerifier {

    private final Class<? extends LongFunction<B>> target;

    public LongMutantVerifier(GregorMutater engine, Class<? extends LongFunction<B>> target, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        super(engine, target, filter, checkUnmutatedValues);
        this.target = target;
    }

    public void firstMutantShouldReturn(long l, B expected) {
        firstMutantShouldReturn(() -> l, expected);
    }

    public void firstMutantShouldReturn(LongSupplier ls, B expected) {

        long input = ls.getAsLong();

        if (checkUnmutated()) {
            assertThat(runWithoutMutation(input))
                    .describedAs("Expected unmutated code to return different value to mutated code")
                    .isNotEqualTo(expected);
        }

        List<MutationDetails> mutations = findMutations();
        assertThat(mutateAndCall(input, getFirstMutant(mutations)))
                .isEqualTo(expected);
    }

    private B runWithoutMutation(long input) {
        return this.runInClassLoader(target.getClassLoader(), input);
    }

    private B mutateAndCall(long input, Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader, input);
    }

    private B runInClassLoader(ClassLoader loader, long input) {
        try {
            Class<?> forLoader = loader.loadClass(target.getName());

            Constructor c = forLoader.getDeclaredConstructor();
            c.setAccessible(true);
            LongFunction<B> instance = (LongFunction<B>) c.newInstance();
            return instance.apply(input);
        } catch (ReflectiveOperationException ex) {
            throw Unchecked.translateCheckedException(ex);
        }
    }

}
