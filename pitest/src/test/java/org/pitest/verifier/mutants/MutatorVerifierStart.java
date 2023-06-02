package org.pitest.verifier.mutants;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.util.Textifier;
import org.objectweb.asm.util.TraceClassVisitor;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class MutatorVerifierStart {

    private final ClassByteArraySource source;
    private final List<MethodMutatorFactory> mmfs;
    private final Predicate<MethodInfo> filter;
    private final Predicate<MutationDetails> mutantFilter;
    private final boolean checkUnmutatedValues;

    public MutatorVerifierStart(ClassByteArraySource source,
                                List<MethodMutatorFactory> mmfs,
                                Predicate<MethodInfo> filter,
                                Predicate<MutationDetails> mutantFilter,
                                boolean checkUnmutatedValues) {
        this.source = source;
        this.mmfs = mmfs;
        this.filter = filter;
        this.mutantFilter = mutantFilter;
        this.checkUnmutatedValues = checkUnmutatedValues;
    }

    public static MutatorVerifierStart forMutator(Collection<MethodMutatorFactory> ms) {
        return new MutatorVerifierStart(ClassloaderByteArraySource.fromContext(),
                ms.stream().collect(Collectors.toList()), method -> true, mf -> true, true);
    }

    public static MutatorVerifierStart forMutator(MethodMutatorFactory... m) {
        return new MutatorVerifierStart(ClassloaderByteArraySource.fromContext(),
                asList(m), method -> true, mf -> true, true);
    }

    public MutatorVerifierStart withByteArraySource(ClassByteArraySource source) {
        return new MutatorVerifierStart(source, mmfs, filter, mutantFilter, checkUnmutatedValues);
    }

    public MutatorVerifierStart mutatingOnly(Predicate<MethodInfo> filter) {
        return new MutatorVerifierStart(source, mmfs, filter, mutantFilter, checkUnmutatedValues);
    }

    public MutatorVerifierStart consideringOnlyMutantsMatching(Predicate<MutationDetails> mutantFilter) {
        return new MutatorVerifierStart(source, mmfs, filter, mutantFilter, checkUnmutatedValues);
    }

    public MutatorVerifierStart notCheckingUnMutatedValues() {
        return new MutatorVerifierStart(source, mmfs, filter, mutantFilter, false);
    }

    public MutatorVerifier forClass(Class<?> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new MutatorVerifier(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    public MutatorVerifier forClass(String clazz) {
        GregorMutater engine = makeEngine();
        return new MutatorVerifier(engine, ClassName.fromString(clazz), mutantFilter, checkUnmutatedValues);
    }

    public <B> CallableMutantVerifier<B> forCallableClass(Class<? extends Callable<B>> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new CallableMutantVerifier<B>(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    public <A,B> MutantVerifier<A,B> forFunctionClass(Class<? extends Function<A,B>> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new MutantVerifier<A,B>(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    public <B> IntMutantVerifier<B> forIntFunctionClass(Class<? extends IntFunction<B>> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new IntMutantVerifier<>(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    public <B> LongMutantVerifier<B> forLongFunctionClass(Class<? extends LongFunction<B>> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new LongMutantVerifier<>(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    public <B> DoubleMutantVerifier<B> forDoubleFunctionClass(Class<? extends DoubleFunction<B>> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new DoubleMutantVerifier<>(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    public <A,B,C> BiFunctionMutantVerifier<A,B,C> forBiFunctionClass(Class<? extends BiFunction<A,B,C>> clazz) {
        assertScanDoesNotAlterClass(clazz);
        GregorMutater engine = makeEngine();
        return new BiFunctionMutantVerifier<>(engine, clazz, mutantFilter, checkUnmutatedValues);
    }

    private GregorMutater makeEngine() {
        // sandwich between NullMutators as a (non robust) check
        // that the mutator provides a unique id
        List<MethodMutatorFactory> mutators = new ArrayList<>();
        mutators.add(new NullMutator());
        mutators.addAll(mmfs);
        mutators.add(new NullMutator());
        return new GregorMutater(source, filter, mutators);
    }

    public void assertScanDoesNotAlterClass(Class<?> clazz) {
        ClassByteArraySource source = ClassloaderByteArraySource.fromContext();
        byte[] bytes = source.getBytes(clazz.getName()).get();

        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassReader reader = new ClassReader(bytes);
        final ScanningClassVisitor nv = new ScanningClassVisitor(writer, mmfs);

        reader.accept(nv, ClassReader.EXPAND_FRAMES);

        assertThat(asString(writer.toByteArray()))
                .isEqualTo(asString(plainTransform(bytes)))
                .describedAs("Mutator modified class when mutation was not active");

    }

    private byte[] plainTransform(byte[] bytes) {
        ClassWriter writer = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        final ClassReader reader = new ClassReader(bytes);
        reader.accept(writer, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }

    private String asString(byte[] bytes) {
        ClassReader reader = new ClassReader(bytes);
        StringWriter writer = new StringWriter();
        reader.accept(new TraceClassVisitor(null, new Textifier(), new PrintWriter(
                writer)), ClassReader.EXPAND_FRAMES);
        return writer.toString();
    }

}
