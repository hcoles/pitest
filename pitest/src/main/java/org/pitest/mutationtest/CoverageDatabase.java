package org.pitest.mutationtest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.pitest.functional.FunctionalCollection;
import org.pitest.mutationtest.instrument.CoverageSource;

public interface CoverageDatabase {

  Map<ClassGrouping, List<String>> mapCodeToTests(
      FunctionalCollection<Class<?>> tests,
      Map<String, ClassGrouping> groupedByOuterClass) throws IOException;

  void initialise(final FunctionalCollection<Class<?>> tests);

  CoverageSource getCoverage(ClassGrouping code, List<String> tests);

}
