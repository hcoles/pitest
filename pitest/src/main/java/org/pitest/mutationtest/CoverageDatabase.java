package org.pitest.mutationtest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.Description;
import org.pitest.classinfo.ClassInfo;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.mutationtest.instrument.CoverageSource;

public interface CoverageDatabase {

  Map<ClassGrouping, List<String>> mapCodeToTests() throws IOException;

  boolean initialise();

  CoverageSource getCoverage(ClassGrouping code, List<String> tests);

  Collection<String> getParentClassesWithoutATest();

  Collection<Description> getTestForLineNumber(ClassLine classLine);

  Collection<ClassInfo> getClassInfo(Collection<String> classesForSourceFile);

  int getNumberOfCoveredLines(Collection<String> mutatedClass);

}
