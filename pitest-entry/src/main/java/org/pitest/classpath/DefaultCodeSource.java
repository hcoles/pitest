package org.pitest.classpath;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.NameToClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.classinfo.TestToClassMapper;
import org.pitest.functional.Streams;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DefaultCodeSource implements CodeSource {
    private final ProjectClassPaths   classPath;
    private final Repository classRepository;

    public DefaultCodeSource(final ProjectClassPaths classPath) {
        this(classPath, new Repository(new ClassPathByteArraySource(
                classPath.getClassPath())));
    }

    public DefaultCodeSource(final ProjectClassPaths classPath,
               final Repository classRepository) {
        this.classPath = classPath;
        this.classRepository = classRepository;
    }

    public Stream<ClassTree> codeTrees() {
        return this.classPath.code().stream()
                .map(c -> this.getBytes(c.asJavaName()))
                .filter(Optional::isPresent)
                .map(maybe -> ClassTree.fromBytes(maybe.get()));
    }

    public Set<ClassName> getCodeUnderTestNames() {
        return this.classPath.code().stream().collect(Collectors.toSet());
    }

    public Stream<ClassTree> testTrees() {
        return this.classPath.test().stream()
                .map(c -> this.getBytes(c.asJavaName()))
                .filter(Optional::isPresent)
                .map(maybe -> ClassTree.fromBytes(maybe.get()))
                .filter(t -> !t.isAbstract());
    }

    public ClassPath getClassPath() {
        return this.classPath.getClassPath();
    }

    public Optional<ClassName> findTestee(final String className) {
        final TestToClassMapper mapper = new TestToClassMapper(this.classRepository);
        return mapper.findTestee(className);
    }

    public Collection<ClassInfo> getClassInfo(final Collection<ClassName> classes) {
        return classes.stream()
                .flatMap(nameToClassInfo())
                .collect(Collectors.toList());
    }

    public Optional<byte[]> fetchClassBytes(final ClassName clazz) {
        return this.classRepository.querySource(clazz);
    }

    @Override
    public Optional<ClassInfo> fetchClass(final ClassName clazz) {
        return this.classRepository.fetchClass(clazz);
    }

    private Function<ClassName, Stream<ClassInfo>> nameToClassInfo() {
        return new NameToClassInfo(this.classRepository)
                .andThen(Streams::fromOptional);
    }

    @Override
    public Optional<byte[]> getBytes(String clazz) {
        return fetchClassBytes(ClassName.fromString(clazz));
    }
}
