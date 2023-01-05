package org.pitest.verifier.mutants;

import org.assertj.core.api.StringAssert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.simpletest.ExcludedPrefixIsolationStrategy;
import org.pitest.simpletest.Transformation;
import org.pitest.simpletest.TransformingClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on class type only
 */
public class MutatorVerifier {

    private final GregorMutater engine;
    private final ClassName clazz;
    private final Predicate<MutationDetails> filter;
    private final boolean checkUnmutatedValues;

    public MutatorVerifier(GregorMutater engine, Class<?> clazz, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        this(engine, ClassName.fromClass(clazz), filter, checkUnmutatedValues);
    }

    public MutatorVerifier(GregorMutater engine, ClassName clazz, Predicate<MutationDetails> filter, boolean checkUnmutatedValues) {
        this.engine = engine;
        this.clazz = clazz;
        this.filter = filter;
        this.checkUnmutatedValues = checkUnmutatedValues;
    }

    public void createsNMutants(int n) {
        assertThat(findMutations()).hasSize(n);
    }

    public void noMutantsCreated() {
        assertThat(findMutations())
                .as(() -> "Expecting no mutants to be generated for " + printClass(clazz))
                .isEmpty();
    }

    public final StringAssert firstMutantDescription() {
        return new StringAssert(firstMutant().getDescription());
    }

    public final List<MutationDetails> findMutations() {
        return this.engine.findMutations(clazz).stream()
                .filter(filter)
                .collect(Collectors.toList());
    }


    public final MutationDetails firstMutant() {
        List<MutationDetails> mutants = findMutations();
        assertThat(mutants)
                .describedAs("No mutations created")
                .isNotEmpty();
        return mutants.get(0);
    }

    protected void verifyMutant(Mutant mutant) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        CheckClassAdapter.verify(new ClassReader(mutant.getBytes()), false, pw);
        assertThat(sw.toString())
                .describedAs("Mutant is not a valid class")
                .isEmpty();
    }

    protected ClassLoader createClassLoader(Mutant mutant) {
        return new TransformingClassLoader(new ClassPath(),
                this.createTransformation(mutant),
                new ExcludedPrefixIsolationStrategy(new String[0]),
                Object.class.getClassLoader());
    }

    private Transformation createTransformation(Mutant mutant) {
        return (name, bytes) -> name.equals(mutant.getDetails().getClassName().asJavaName()) ? mutant.getBytes() : bytes;
    }

    protected final Mutant getFirstMutant(final Collection<MutationDetails> actual) {
        assertThat(actual)
                .as(() -> "Expecting at least one mutant to be generated for " + printClass(clazz))
                .isNotEmpty();
        final Mutant mutant = this.engine.getMutation(actual.iterator().next()
                .getId());
        verifyMutant(mutant);
        return mutant;
    }

    protected String printMutant(Mutant mutant) {
        return print(mutant.getBytes());
    }

    protected String printClass(ClassName clazz) {
        byte[] bytes = ClassloaderByteArraySource.fromContext().getBytes(clazz.asInternalName())
                .get();
        return print(bytes);
    }

    protected String print(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        reader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(bos)), 8);
        return bos.toString();
    }


    protected boolean checkUnmutated() {
        return this.checkUnmutatedValues;
    }
}
