package org.pitest.verifier.interceptors;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.util.ArrayList;
import java.util.List;

public class InterceptorVerifier {

    private final List<MethodMutatorFactory> mutators = new ArrayList<>();
    private final MutationInterceptor testee;

    InterceptorVerifier(MutationInterceptor testee) {
        this.testee = testee;
    }

    public InterceptorVerifier usingMutator(MethodMutatorFactory mutator) {
        mutators.add(mutator);
        return this;
    }

    public Verifier forClass(Class<?> clazz) {
        final Sample s = makeSampleForCurrentCompiler(clazz);
        Mutater m = mutateFromClassLoader();
        return new Verifier(s, testee, m);
    }

    public ResourceFolderInterceptorVerifier usingResourceFolder(String path) {
        return new ResourceFolderInterceptorVerifier(mutators, testee, path);
    }

    private Sample makeSampleForCurrentCompiler(Class<?> clazz) {
        final ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
        return new Sample(ClassName.fromClass(clazz),
                ClassTree.fromBytes(source.getBytes(clazz.getName()).get()));
    }

    private GregorMutater mutateFromClassLoader() {
        return new GregorMutater(ClassloaderByteArraySource.fromContext(), m -> true, this.mutators);
    }
}
