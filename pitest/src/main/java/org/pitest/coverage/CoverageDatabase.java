package org.pitest.coverage;

import java.math.BigInteger;
import java.util.Collection;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;

public interface CoverageDatabase {

  Collection<ClassInfo> getClassInfo(Collection<ClassName> classes);

  int getNumberOfCoveredLines(Collection<ClassName> clazz);

  Collection<TestInfo> getTestsForClass(ClassName clazz);

  Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

  BigInteger getCoverageIdForClass(ClassName clazz);

  Collection<ClassInfo> getClassesForFile(String sourceFile, String packageName);

  CoverageSummary createSummary();

}
