package org.pitest.verifier.mutants;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.util.Unchecked;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on instantiating mutated versions of a class.
 * <p>
 * Classes must implement java.util.Function and provide a default
 * constructor.
 */
public class CallableMutantVerifier<B> extends MutatorVerifier {

    private final Class<? extends Callable<B>> target;

    public CallableMutantVerifier(GregorMutater engine, Class<? extends Callable<B>> target, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        super(engine, target, filter, checkUnmutatedValues);
        this.target = target;
    }


    public B firstMutantReturnValue() {
        List<MutationDetails> mutations = findMutations();
        return mutateAndCall(getFirstMutant(mutations));
    }

    /**
     * Suppliers allow consumable inputs (eg streams) can be reused
     */
    public void firstMutantShouldReturn(B expected) {
        if (checkUnmutated()) {
            assertThat(runWithoutMutation())
                    .describedAs("Expected unmutated code to return different value to mutated code")
                    .isNotEqualTo(expected);
        }

        List<MutationDetails> mutations = findMutations();
        Mutant mutant = getFirstMutant(mutations);
        assertThat(mutateAndCall(mutant))
                .as(() -> "Unexpected return value from mutant\n " + printMutant(mutant))
                .isEqualTo(expected);
    }

    private B runWithoutMutation() {
        return this.runInClassLoader(target.getClassLoader());
    }

    private B mutateAndCall(Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader);
    }

    private B runInClassLoader(ClassLoader loader) {
        try {
            Class<?> forLoader = loader.loadClass(target.getName());

            Constructor c = forLoader.getDeclaredConstructor();
            c.setAccessible(true);
            Callable<B> instance = (Callable<B>) c.newInstance();
            return instance.call();
        } catch (ReflectiveOperationException ex) {
            throw Unchecked.translateCheckedException(ex);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
