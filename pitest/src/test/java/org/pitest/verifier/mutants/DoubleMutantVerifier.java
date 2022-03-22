package org.pitest.verifier.mutants;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.simpletest.ExcludedPrefixIsolationStrategy;
import org.pitest.simpletest.Transformation;
import org.pitest.simpletest.TransformingClassLoader;
import org.pitest.util.Unchecked;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.function.DoubleSupplier;
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
public class DoubleMutantVerifier<B> extends MutatorVerifier {

    private final GregorMutater engine;
    private final Class<? extends DoubleFunction<B>> target;

    public DoubleMutantVerifier(GregorMutater engine, Class<? extends DoubleFunction<B>> target, Predicate<MutationDetails> filter) {
        super(engine, target, filter);
        this.engine = engine;
        this.target = target;
    }

    /**
     * Suppliers allow consumable inputs (eg streams) can be reused
     */
    public void firstMutantShouldReturn(DoubleSupplier input, B expected) {
        assertThat(runWithoutMutation(input.getAsDouble()))
                .describedAs("Expected unmutated code to return different value to mutated code")
                .isNotEqualTo(expected);

        List<MutationDetails> mutations = findMutations();
        assertThat(mutateAndCall(input.getAsDouble(), getFirstMutant(mutations)))
                .isEqualTo(expected);
    }

    private B runWithoutMutation(double input) {
        return this.runInClassLoader(target.getClassLoader(), input);
    }

    private B mutateAndCall(double input, Mutant mutant) {
        ClassLoader loader = this.createClassLoader(mutant);
        return this.runInClassLoader(loader, input);
    }

    private ClassLoader createClassLoader(Mutant mutant) {
        return new TransformingClassLoader(new ClassPath(),
                this.createTransformation(mutant),
                new ExcludedPrefixIsolationStrategy(new String[0]),
                Object.class.getClassLoader());
    }

    private Transformation createTransformation(Mutant mutant) {
        return (name, bytes) -> name.equals(mutant.getDetails().getClassName().asJavaName()) ? mutant.getBytes() : bytes;
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

    protected Mutant getFirstMutant(final Collection<MutationDetails> actual) {
        assertThat(actual)
                .describedAs("Expecting at least one mutant to be generated")
                .isNotEmpty();
        final Mutant mutant = this.engine.getMutation(actual.iterator().next()
                .getId());
        verifyMutant(mutant);
        return mutant;
    }

    private void verifyMutant(Mutant mutant) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(mutant.getBytes()), false, pw);
        assertThat(sw.toString())
                .describedAs("Mutant is not a valid class")
                .isEmpty();
    }

}
