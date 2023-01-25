package org.pitest.classpath;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoSource;
import org.pitest.classinfo.ClassName;

/**
 * Provides access to code and tests on the classpath
 */
public interface CodeSource extends ClassInfoSource, ClassByteArraySource {

  Stream<ClassTree> codeTrees();

  Set<ClassName> getCodeUnderTestNames();

  Stream<ClassTree> testTrees();

  ClassPath getClassPath();

  Optional<ClassName> findTestee(String className);

  Optional<byte[]> fetchClassBytes(ClassName clazz);

  @Override
  Optional<ClassInfo> fetchClass(ClassName clazz);
  Collection<ClassInfo> getClassInfo(Collection<ClassName> classes);

  @Override
  Optional<byte[]> getBytes(String clazz);
}
