package org.pitest.mutationtest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.Description;
import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.mutationtest.instrument.ClassLine;

public interface CoverageDatabase {

  Map<ClassGrouping, List<String>> mapCodeToTests() throws IOException;

  boolean initialise();

  Collection<String> getParentClassesWithoutATest();

  Collection<Description> getTestForLineNumber(ClassLine classLine);

  Collection<ClassInfo> getClassInfo(Collection<String> classesForSourceFile);

  int getNumberOfCoveredLines(Collection<String> mutatedClass);

  Collection<TestInfo> getTestsForMutant(MutationDetails mutation);

}
