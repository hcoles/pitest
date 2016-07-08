package org.pitest.classpath;

import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.prelude.Prelude.and;
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
import org.pitest.testapi.TestClassIdentifier;

/**
 * Provides access to code and tests on the classpath
 */
public class CodeSource implements ClassInfoSource {

  private final ProjectClassPaths   classPath;
  private final Repository          classRepository;
  private final TestClassIdentifier testIdentifier;

  public CodeSource(final ProjectClassPaths classPath,
      final TestClassIdentifier testIdentifier) {
    this(classPath, new Repository(new ClassPathByteArraySource(
        classPath.getClassPath())), testIdentifier);
  }

  CodeSource(final ProjectClassPaths classPath,
      final Repository classRepository, final TestClassIdentifier testIdentifier) {
    this.classPath = classPath;
    this.classRepository = classRepository;
    this.testIdentifier = testIdentifier;
  }

  public Collection<ClassInfo> getCode() {
    return FCollection.flatMap(this.classPath.code(), nameToClassInfo())
        .filter(not(isWithinATestClass()));
  }

  public Set<ClassName> getCodeUnderTestNames() {
    final Set<ClassName> codeClasses = new HashSet<ClassName>();
    FCollection.mapTo(getCode(), ClassInfo.toClassName(), codeClasses);
    return codeClasses;
  }

  @SuppressWarnings("unchecked")
  public List<ClassInfo> getTests() {
    return flatMap(this.classPath.test(), nameToClassInfo()).filter(
        and(isWithinATestClass(), isIncludedClass(),
            not(ClassInfo.matchIfAbstract())));
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

  private F<ClassInfo, Boolean> isWithinATestClass() {
    return new F<ClassInfo, Boolean>() {

      @Override
      public Boolean apply(final ClassInfo a) {
        return CodeSource.this.testIdentifier.isATestClass(a);
      }

    };

  }

  private F<ClassInfo, Boolean> isIncludedClass() {
    return new F<ClassInfo, Boolean>() {
      @Override
      public Boolean apply(final ClassInfo a) {
        return CodeSource.this.testIdentifier.isIncluded(a);
      }

    };

  }
}
