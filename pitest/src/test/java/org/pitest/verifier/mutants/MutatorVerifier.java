package org.pitest.verifier.mutants;

import org.assertj.core.api.StringAssert;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.simpletest.ExcludedPrefixIsolationStrategy;
import org.pitest.simpletest.Transformation;
import org.pitest.simpletest.TransformingClassLoader;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verification based on class type only
 */
public class MutatorVerifier {

    private final GregorMutater engine;
    private final Class<?> clazz;
    private Predicate<MutationDetails> filter;

    public MutatorVerifier(GregorMutater engine, Class<?> clazz, Predicate<MutationDetails> filter) {
        this.engine = engine;
        this.clazz = clazz;
        this.filter = filter;
    }

    public void createsNMutants(int n) {
        assertThat(findMutations()).hasSize(n);
    }

    public void noMutantsCreated() {
        assertThat(findMutations()).isEmpty();
    }

    public final StringAssert firstMutantIsDescription() {
        return new StringAssert(firstMutant().getDescription());
    }

    public final List<MutationDetails> findMutations() {
        return this.engine.findMutations(ClassName.fromClass(clazz)).stream()
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

    protected String printMutant(Mutant mutant) {
        ClassReader reader = new ClassReader(mutant.getBytes());
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        reader.accept(new TraceClassVisitor(null, new ASMifier(), new PrintWriter(bos)), 8);
        return bos.toString();
    }
}
