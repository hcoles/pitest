package org.pitest.mutationtest;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassHash;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.classpath.CodeSource;
import org.pitest.util.IsolationUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FixedCodeSource implements CodeSource {

    private final List<ClassTree> classes;

    public FixedCodeSource(Class<?> ... classes) {
        this(Arrays.stream(classes)
                .map(FixedCodeSource::toTree).collect(Collectors.toList()));
    }

    public FixedCodeSource(List<ClassTree> classes) {
        this.classes = classes;
    }

    private static ClassTree toTree(Class<?> aClass) {
        ClassloaderByteArraySource cba = new ClassloaderByteArraySource(IsolationUtils.getContextClassLoader());
        return ClassTree.fromBytes(cba.getBytes(aClass.getName()).get());
    }

    @Override
    public Stream<ClassTree> codeTrees() {
        return classes.stream();
    }

    @Override
    public Set<ClassName> getCodeUnderTestNames() {
        return classes.stream()
                .map(ClassTree::name)
                .collect(Collectors.toSet());
    }

    @Override
    public Set<ClassName> getTestClassNames() {
        return Collections.emptySet();
    }

    @Override
    public Stream<ClassTree> testTrees() {
        return Stream.empty();
    }

    @Override
    public ClassPath getClassPath() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ClassName> findTestee(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<byte[]> fetchClassBytes(ClassName clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<ClassHash> fetchClassHash(ClassName clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<ClassHash> fetchClassHashes(Collection<ClassName> classes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Optional<byte[]> getBytes(String clazz) {
        throw new UnsupportedOperationException();
    }
}
