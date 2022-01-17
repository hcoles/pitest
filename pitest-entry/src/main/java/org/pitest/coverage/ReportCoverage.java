package org.pitest.coverage;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;

import java.util.Collection;

/**
 * Subset of coverage interface used by legacy html report
 */
public interface ReportCoverage {
    Collection<ClassInfo> getClassInfo(Collection<ClassName> classes);

    int getNumberOfCoveredLines(Collection<ClassName> clazz);

    Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

    Collection<ClassInfo> getClassesForFile(String sourceFile, String packageName);

}
