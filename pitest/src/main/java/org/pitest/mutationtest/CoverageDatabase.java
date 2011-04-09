package org.pitest.mutationtest;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.pitest.mutationtest.instrument.CoverageSource;

public interface CoverageDatabase {

  Map<ClassGrouping, List<String>> mapCodeToTests(
      Map<String, ClassGrouping> groupedByOuterClass) throws IOException;

  boolean initialise(final Collection<Class<?>> tests);

  CoverageSource getCoverage(ClassGrouping code, List<String> tests);

}
