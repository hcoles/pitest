package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

import java.math.BigInteger;
import java.util.Collection;

public interface CoverageDatabase extends ReportCoverage {

  Collection<TestInfo> getTestsForClass(ClassName clazz);

  Collection<TestInfo> getTestsForBlockLocation(BlockLocation location);

  BigInteger getCoverageIdForClass(ClassName clazz);

}
