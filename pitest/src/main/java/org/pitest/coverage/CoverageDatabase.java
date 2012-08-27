package org.pitest.coverage;

import java.util.Collection;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.mutationtest.instrument.ClassLine;

public interface CoverageDatabase {

  Collection<ClassInfo> getClassInfo(Collection<ClassName> classesForSourceFile);

  int getNumberOfCoveredLines(Collection<ClassName> mutatedClass);

  Collection<TestInfo> getTestsForClass(ClassName clazz);

  Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

}
