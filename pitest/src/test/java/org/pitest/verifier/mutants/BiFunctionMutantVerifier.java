package org.pitest.verifier.mutants;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.util.Unchecked;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;

public class BiFunctionMutantVerifier<A, B, C> extends MutatorVerifier {

    private final Class<? extends BiFunction<A, B, C>> target;

    public BiFunctionMutantVerifier(GregorMutater engine,
                                    Class<? extends BiFunction<A, B, C>> target,
                                    Predicate<MutationDetails> filter,
                                    boolean checkUnmutatedValues) {
        super(engine, target, filter, checkUnmutatedValues);
        this.target = target;
    }

    public void firstMutantShouldReturn(A a, B b, C expected) {
        firstMutantShouldReturn( () -> a, () -> b, expected);
    }

    /**
     * Suppliers allow consumable inputs (eg streams) can be reused
     */
    public void firstMutantShouldReturn(Supplier<A> as, Supplier<B> bs, C expected) {
        A a = as.get();
        B b = bs.get();
        if (checkUnmutated()) {
            assertThat(runWithoutMutation(a,b))
                    .describedAs("Expected unmutated code to return different value to mutated code")
                    .isNotEqualTo(expected);
        }

        List<MutationDetails> mutations = findMutations();
        assertThat(mutateAndCall(a, b, getFirstMutant(mutations)))
                .isEqualTo(expected);
    }


    private C runWithoutMutation(A a, B b) {
        return this.runInClassLoader(target.getClassLoader(), a, b);
    }

    private C mutateAndCall(A a, B b, Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader, a, b);
    }

    private C runInClassLoader(ClassLoader loader, A a, B b) {
        try {
            Class<?> forLoader = loader.loadClass(target.getName());

            Constructor c = forLoader.getDeclaredConstructor();
            c.setAccessible(true);
            BiFunction<A, B, C> instance = (BiFunction<A, B, C>) c.newInstance();
            return instance.apply(a, b);
        } catch (ReflectiveOperationException ex) {
            throw Unchecked.translateCheckedException(ex);
        }
    }

}
