package org.pitest.coverage;

import java.util.Collection;

import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.mutationtest.instrument.ClassLine;

public interface CoverageDatabase {

  Collection<ClassInfo> getClassInfo(Collection<String> classesForSourceFile);

  int getNumberOfCoveredLines(Collection<String> mutatedClass);

  Collection<TestInfo> getTestsForClass(String clazz);

  Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

}
