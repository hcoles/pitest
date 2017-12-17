package org.pitest.classpath;

import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.prelude.Prelude.not;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.NameToClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.classinfo.TestToClassMapper;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;

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
    return FCollection.flatMap(this.classPath.code(), nameToClassInfo());
  }

  public Set<ClassName> getCodeUnderTestNames() {
    final Set<ClassName> codeClasses = new HashSet<>();
    FCollection.mapTo(getCode(), ClassInfo.toClassName(), codeClasses);
    return codeClasses;
  }

  public List<ClassInfo> getTests() {
    return flatMap(this.classPath.test(), nameToClassInfo()).filter(
                    not(ClassInfo.matchIfAbstract()));
  }

  public ClassPath getClassPath() {
    return this.classPath.getClassPath();
  }

  public ProjectClassPaths getProjectPaths() {
    return this.classPath;
  }

  public Option<ClassName> findTestee(final String className) {
    final TestToClassMapper mapper = new TestToClassMapper(this.classRepository);
    return mapper.findTestee(className);
  }

  public Collection<ClassInfo> getClassInfo(final Collection<ClassName> classes) {
    return FCollection.flatMap(classes, nameToClassInfo());
  }

  // not used but keep to allow plugins to query bytecode
  public Option<byte[]> fetchClassBytes(final ClassName clazz) {
    return this.classRepository.querySource(clazz);
  }

  @Override
  public Option<ClassInfo> fetchClass(final ClassName clazz) {
    return this.classRepository.fetchClass(clazz);
  }

  private F<ClassName, Option<ClassInfo>> nameToClassInfo() {
    return new NameToClassInfo(this.classRepository);
  }

}
