package org.pitest.classinfo;

import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.Prelude.and;
import static org.pitest.functional.Prelude.not;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.extension.TestClassIdentifier;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.MutationClassPaths;

/**
 * Provides access to code and tests on the classpath
 */
public class CodeSource implements ClassInfoSource {

  private final MutationClassPaths  classPath;
  private final Repository          classRepository;
  private final TestClassIdentifier testIdentifier;

  public CodeSource(final MutationClassPaths classPath,
      final TestClassIdentifier testIdentifier) {
    this(classPath, new Repository(new ClassPathByteArraySource(
        classPath.getClassPath())), testIdentifier);
  }

  CodeSource(final MutationClassPaths classPath,
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
        and(isWithinATestClass(), not(ClassInfo.matchIfAbstract())));
  }

  public ClassPath getClassPath() {
    return this.classPath.getClassPath();
  }

  public Option<ClassName> findTestee(final String className) {
    final TestToClassMapper mapper = new TestToClassMapper(this.classRepository);
    return mapper.findTestee(className);
  }

  public Collection<ClassInfo> getClassInfo(final Collection<ClassName> classes) {
    return FCollection.flatMap(classes, nameToClassInfo());
  }

  public Option<ClassInfo> fetchClass(final ClassName clazz) {
    return this.classRepository.fetchClass(clazz);
  }

  private F<ClassName, Option<ClassInfo>> nameToClassInfo() {
    return new NameToClassInfo(this.classRepository);
  }

  private F<ClassInfo, Boolean> isWithinATestClass() {
    return new F<ClassInfo, Boolean>() {

      public Boolean apply(final ClassInfo a) {
        System.out.println(a);
        return CodeSource.this.testIdentifier.isATestClass(a);
      }

    };

  }

}
