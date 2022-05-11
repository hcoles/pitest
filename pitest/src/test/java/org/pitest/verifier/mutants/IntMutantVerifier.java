package org.pitest.verifier.mutants;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.util.Unchecked;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on instantiating mutated versions of a class.
 * <p>
 * Classes must implement java.util.IntFunction and provide a default
 * constructor.
 */
public class IntMutantVerifier<B> extends MutatorVerifier {

    private final Class<? extends IntFunction<B>> target;

    public IntMutantVerifier(GregorMutater engine, Class<? extends IntFunction<B>> target, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        super(engine, target, filter, checkUnmutatedValues);
        this.target = target;
    }

    public void firstMutantShouldReturn(int input, B expected) {
        firstMutantShouldReturn(() -> input, expected);
    }

    public void firstMutantShouldReturn(IntSupplier is, B expected) {
        int input = is.getAsInt();

        if (checkUnmutated()) {
            assertThat(runWithoutMutation(input))
                    .describedAs("Expected unmutated code to return different value to mutated code")
                    .isNotEqualTo(expected);
        }

        List<MutationDetails> mutations = findMutations();
        Mutant mutant = getFirstMutant(mutations);
        assertThat(mutateAndCall(input, mutant))
                .as(() -> "Unexpected return value from mutant\n " + printMutant(mutant))
                .isEqualTo(expected);
    }

    private B runWithoutMutation(int input) {
        return this.runInClassLoader(target.getClassLoader(), input);
    }

    private B mutateAndCall(int input, Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader, input);
    }

    private B runInClassLoader(ClassLoader loader, int input) {
        try {
            Class<?> forLoader = loader.loadClass(target.getName());

            Constructor c = forLoader.getDeclaredConstructor();
            c.setAccessible(true);
            IntFunction<B> instance = (IntFunction<B>) c.newInstance();
            return instance.apply(input);
        } catch (ReflectiveOperationException ex) {
            throw Unchecked.translateCheckedException(ex);
        }
    }


}
