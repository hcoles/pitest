package org.pitest.coverage;

import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;

public class NoCoverage implements CoverageDatabase {
    @Override
    public Collection<ClassInfo> getClassInfo(Collection<ClassName> classes) {
        return Collections.emptyList();
    }

    @Override
    public int getNumberOfCoveredLines(Collection<ClassName> clazz) {
        return 0;
    }

    @Override
    public Collection<TestInfo> getTestsForClass(ClassName clazz) {
        return Collections.emptyList();
    }

    @Override
    public Collection<TestInfo> getTestsForBlockLocation(BlockLocation location) {
        return Collections.emptyList();
    }

    @Override
    public Collection<TestInfo> getTestsForClassLine(ClassLine classLine) {
        return Collections.emptyList();
    }

    @Override
    public BigInteger getCoverageIdForClass(ClassName clazz) {
        return BigInteger.ZERO;
    }

    @Override
    public Collection<ClassInfo> getClassesForFile(String sourceFile, String packageName) {
        return Collections.emptyList();
    }

    @Override
    public CoverageSummary createSummary() {
        return new CoverageSummary(0,0);
    }

}
