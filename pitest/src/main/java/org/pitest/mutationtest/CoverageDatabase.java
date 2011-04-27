package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.mutationtest.instrument.ClassLine;

public interface CoverageDatabase {

  boolean initialise();

  Collection<String> getParentClassesWithoutATest();

  Collection<TestInfo> getTestForLineNumber(ClassLine classLine);

  Collection<ClassInfo> getClassInfo(Collection<String> classesForSourceFile);

  int getNumberOfCoveredLines(Collection<String> mutatedClass);

  Collection<TestInfo> getTestsForMutant(MutationDetails mutation);

  Collection<ClassGrouping> getGroupedClasses();

}
