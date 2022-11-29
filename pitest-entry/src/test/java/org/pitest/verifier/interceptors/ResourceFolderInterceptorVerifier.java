package org.pitest.verifier.interceptors;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.gregor.GregorMutater;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.util.ResourceFolderByteArraySource;

import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

public class ResourceFolderInterceptorVerifier {
    private final ClassByteArraySource source = new ResourceFolderByteArraySource();
    private final List<MethodMutatorFactory> mutators;
    private final MutationInterceptor testee;
    private final String path;

    public ResourceFolderInterceptorVerifier(List<MethodMutatorFactory> mutators, MutationInterceptor testee, String path) {
        this.mutators = mutators;
        this.testee = testee;
        this.path = path;
    }

    public Verifier forClass(String clazz) {
        final Sample s = makeSample(clazz);
        Mutater m = mutateFromResourceDir();
        return new Verifier(s, testee, m);
    }

    private Sample makeSample(String sample) {
        final String clazz = makeClassName(sample);
        final Optional<byte[]> bs = source.getBytes(clazz);
        if (bs.isPresent()) {
            return new Sample(ClassName.fromString(clazz), ClassTree.fromBytes(bs.get()));
        }
        throw new RuntimeException("Could not find " + sample);

    }

    private String makeClassName(String sample) {
        return this.path + "/" + sample;
    }

    private GregorMutater mutateFromResourceDir() {
        return new GregorMutater(this.source, m -> true, this.mutators);
    }
}
