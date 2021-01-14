package org.pitest.coverage;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public interface CoverageDatabase {

  Collection<ClassInfo> getClassInfo(Collection<ClassName> classes);

  int getNumberOfCoveredLines(Collection<ClassName> clazz);

  Collection<TestInfo> getTestsForClass(ClassName clazz);

  Collection<TestInfo> getTestsForInstructionLocation(InstructionLocation location);

  Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

  BigInteger getCoverageIdForClass(ClassName clazz);

  Collection<ClassInfo> getClassesForFile(String sourceFile, String packageName);

  CoverageSummary createSummary();

  Map<InstructionLocation, Set<TestInfo>> getInstructionCoverage();


}
