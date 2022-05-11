package org.pitest.verifier.mutants;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.util.Unchecked;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on instantiating mutated versions of a class.
 * <p>
 * Classes must implement java.util.IntFunction and provide a default
 * constructor.
 */
public class DoubleMutantVerifier<B> extends MutatorVerifier {

    private final Class<? extends DoubleFunction<B>> target;

    public DoubleMutantVerifier(GregorMutater engine, Class<? extends DoubleFunction<B>> target, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        super(engine, target, filter, checkUnmutatedValues);
        this.target = target;
    }

    public void firstMutantShouldReturn(double ds, B expected) {
        firstMutantShouldReturn(() -> ds, expected);
    }

    public void firstMutantShouldReturn(DoubleSupplier ds, B expected) {
        double input = ds.getAsDouble();
        if (checkUnmutated()) {
            assertThat(runWithoutMutation(input))
                    .describedAs("Expected unmutated code to return different value to mutated code")
                    .isNotEqualTo(expected);
        }

        List<MutationDetails> mutations = findMutations();
        assertThat(mutateAndCall(input, getFirstMutant(mutations)))
                .isEqualTo(expected);
    }

    private B runWithoutMutation(double input) {
        return this.runInClassLoader(target.getClassLoader(), input);
    }

    private B mutateAndCall(double input, Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader, input);
    }

    private B runInClassLoader(ClassLoader loader, double input) {
        try {
            Class<?> forLoader = loader.loadClass(target.getName());

            Constructor c = forLoader.getDeclaredConstructor();
            c.setAccessible(true);
            DoubleFunction<B> instance = (DoubleFunction<B>) c.newInstance();
            return instance.apply(input);
        } catch (ReflectiveOperationException ex) {
            throw Unchecked.translateCheckedException(ex);
        }
    }

}
