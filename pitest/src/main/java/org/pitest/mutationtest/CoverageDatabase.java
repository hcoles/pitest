package org.pitest.mutationtest;

import java.util.Collection;

import org.pitest.classinfo.ClassInfo;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.extension.Configuration;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.util.JavaAgent;

public interface CoverageDatabase {

  boolean initialise();

  Collection<TestInfo> getTestForLineNumber(ClassLine classLine);

  Collection<ClassInfo> getClassInfo(Collection<String> classesForSourceFile);

  Collection<ClassInfo> getCodeClasses();

  int getNumberOfCoveredLines(Collection<String> mutatedClass);

  Collection<TestInfo> getTestsForClass(String clazz);

  Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

  Configuration getConfiguration();

  JavaAgent getJavaAgent();

}
