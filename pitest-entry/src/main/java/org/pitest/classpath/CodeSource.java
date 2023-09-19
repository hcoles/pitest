package org.pitest.classpath;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassHash;
import org.pitest.classinfo.ClassHashSource;
import org.pitest.classinfo.ClassName;

/**
 * Provides access to code and tests on the classpath
 */
public interface CodeSource extends ClassHashSource, ClassByteArraySource {

  Stream<ClassTree> codeTrees();

  default Set<ClassName> getAllClassAndTestNames() {
    final Set<ClassName> names = new HashSet<>();
    names.addAll(getCodeUnderTestNames());
    names.addAll(getTestClassNames());
    return names;
  }

  Set<ClassName> getCodeUnderTestNames();

  Set<ClassName> getTestClassNames();

  Stream<ClassTree> testTrees();

  ClassPath getClassPath();

  Optional<ClassName> findTestee(String className);

  Optional<byte[]> fetchClassBytes(ClassName clazz);

  Optional<ClassHash> fetchClassHash(ClassName clazz);

  Collection<ClassHash> fetchClassHashes(Collection<ClassName> classes);

  @Override
  Optional<byte[]> getBytes(String clazz);

}
