package org.pitest.mutationtest;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.pitest.functional.FunctionalCollection;

public interface CoverageDatabase {

  Map<ClassGrouping, List<String>> mapCodeToTests(
      FunctionalCollection<Class<?>> tests,
      Map<String, ClassGrouping> groupedByOuterClass) throws IOException;

}
