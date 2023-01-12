package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

import java.util.Collection;
import java.util.Set;

/**
 * Subset of coverage interface used by legacy html report
 */
public interface ReportCoverage {
    ClassLines getCodeLinesForClass(ClassName clazz);

    Set<ClassLine> getCoveredLines(ClassName clazz);

    Collection<ClassLines> getClassesForFile(String sourceFile, String packageName);

}
