package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

import java.util.Collection;
import java.util.Optional;

/**
 * Subset of coverage interface used by legacy html report
 */
public interface ReportCoverage {
    Optional<ClassLines> getCoveredLinesForClass(ClassName clazz);

    int getNumberOfCoveredLines(Collection<ClassName> clazz);

    Collection<TestInfo> getTestsForClassLine(ClassLine classLine);

    Collection<ClassLines> getClassesForFile(String sourceFile, String packageName);

}
