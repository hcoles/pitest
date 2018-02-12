package org.pitest.classpath;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.NameToClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.classinfo.TestToClassMapper;
import org.pitest.functional.FCollection;
import org.pitest.functional.Streams;

/**
 * Provides access to code and tests on the classpath
 */
public class CodeSource implements ClassInfoSource {

  private final ProjectClassPaths   classPath;
  private final Repository          classRepository;

  public CodeSource(final ProjectClassPaths classPath) {
    this(classPath, new Repository(new ClassPathByteArraySource(
        classPath.getClassPath())));
  }

  CodeSource(final ProjectClassPaths classPath,
      final Repository classRepository) {
    this.classPath = classPath;
    this.classRepository = classRepository;
  }

  public Collection<ClassInfo> getCode() {
    return this.classPath.code().stream()
        .flatMap(nameToClassInfo())
        .collect(Collectors.toList());
  }

  public Set<ClassName> getCodeUnderTestNames() {
    final Set<ClassName> codeClasses = new HashSet<>();
    FCollection.mapTo(getCode(), ClassInfo.toClassName(), codeClasses);
    return codeClasses;
  }

  public List<ClassInfo> getTests() {
    return this.classPath.test().stream()
        .flatMap(nameToClassInfo())
        .filter(ClassInfo.matchIfAbstract().negate())
        .collect(Collectors.toList());
  }

  public ClassPath getClassPath() {
    return this.classPath.getClassPath();
  }

  public ProjectClassPaths getProjectPaths() {
    return this.classPath;
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

  // not used but keep to allow plugins to query bytecode
  public Optional<byte[]> fetchClassBytes(final ClassName clazz) {
    return this.classRepository.querySource(clazz);
  }

  @Override
  public Optional<ClassInfo> fetchClass(final ClassName clazz) {
    return this.classRepository.fetchClass(clazz);
  }

  private Function<ClassName, Stream<ClassInfo>> nameToClassInfo() {
    return new NameToClassInfo(this.classRepository)
        .andThen(opt -> Streams.fromOptional(opt));
  }

}
