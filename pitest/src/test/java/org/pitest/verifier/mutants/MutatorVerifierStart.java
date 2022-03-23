package org.pitest.verifier.mutants;

import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.function.BiFunction;
import java.util.function.DoubleFunction;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;

public class MutatorVerifierStart {

    private final MethodMutatorFactory mmf;
    private final Predicate<MethodInfo> filter;
    private final Predicate<MutationDetails> mutantFilter;

    public MutatorVerifierStart(MethodMutatorFactory mmf, Predicate<MethodInfo> filter, Predicate<MutationDetails> mutantFilter) {
        this.mmf = mmf;
        this.filter = filter;
        this.mutantFilter = mutantFilter;
    }

    public static MutatorVerifierStart forMutator(MethodMutatorFactory m) {
        return new MutatorVerifierStart(m, method -> true, mf -> true);
    }

    public MutatorVerifierStart mutatingOnly(Predicate<MethodInfo> filter) {
        return new MutatorVerifierStart(mmf, filter, mutantFilter);
    }

    public MutatorVerifierStart consideringOnlyMutantsMatching(Predicate<MutationDetails> mutantFilter) {
        return new MutatorVerifierStart(mmf, filter, mutantFilter);
    }

    public MutatorVerifier forClass(Class<?> clazz) {
        GregorMutater engine = makeEngine();
        return new MutatorVerifier(engine, clazz, mutantFilter);
    }

    public <B> CallableMutantVerifier<B> forCallableClass(Class<? extends Callable<B>> clazz) {
        GregorMutater engine = makeEngine();
        return new CallableMutantVerifier<B>(engine, clazz, mutantFilter);
    }

    public <A,B> MutantVerifier<A,B> forFunctionClass(Class<? extends Function<A,B>> clazz) {
        GregorMutater engine = makeEngine();
        return new MutantVerifier<A,B>(engine, clazz, mutantFilter);
    }

    public <B> IntMutantVerifier<B> forIntFunctionClass(Class<? extends IntFunction<B>> clazz) {
        GregorMutater engine = makeEngine();
        return new IntMutantVerifier<>(engine, clazz, mutantFilter);
    }

    public <B> LongMutantVerifier<B> forLongFunctionClass(Class<? extends LongFunction<B>> clazz) {
        GregorMutater engine = makeEngine();
        return new LongMutantVerifier<>(engine, clazz, mutantFilter);
    }

    public <B> DoubleMutantVerifier<B> forDoubleFunctionClass(Class<? extends DoubleFunction<B>> clazz) {
        GregorMutater engine = makeEngine();
        return new DoubleMutantVerifier<>(engine, clazz, mutantFilter);
    }

    public <A,B,C> BiFunctionMutantVerifier<A,B,C> forBiFunctionClass(Class<? extends BiFunction<A,B,C>> clazz) {
        GregorMutater engine = makeEngine();
        return new BiFunctionMutantVerifier<>(engine, clazz, mutantFilter);
    }

    private GregorMutater makeEngine() {
        return new GregorMutater(ClassloaderByteArraySource.fromContext(), filter, Arrays.asList(new NullMutator(), mmf, new NullMutator()));
    }

}
