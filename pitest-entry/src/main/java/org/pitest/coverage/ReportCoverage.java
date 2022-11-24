package org.pitest.coverage;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.classinfo.ClassName;

import java.util.Collection;

/**
 * Subset of coverage interface used by legacy html report
 */
public interface ReportCoverage {
    Collection<ClassTree> getClassInfo(Collection<ClassName> classes);

    int getNumberOfCoveredLines(Collection<ClassName> clazz);

    Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

    Collection<ClassLines> getClassesForFile(String sourceFile, String packageName);

}
