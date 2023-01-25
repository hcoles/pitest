package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class NoCoverage implements CoverageDatabase {
    @Override
    public ClassLines getCodeLinesForClass(ClassName clazz) {
        return new ClassLines(clazz, Collections.emptySet());
    }

    @Override
    public Set<ClassLine> getCoveredLines(ClassName clazz) {
        return Collections.emptySet();
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
    public BigInteger getCoverageIdForClass(ClassName clazz) {
        return BigInteger.ZERO;
    }

    @Override
    public Collection<ClassLines> getClassesForFile(String sourceFile, String packageName) {
        return Collections.emptyList();
    }

}
