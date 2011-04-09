package org.pitest.mutationtest;

import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.FCollection.forEach;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.dependency.DependencyExtractor;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.util.Functions;
import org.pitest.util.Log;

public class DependencyBasedCoverageDatabase {

  private final static Logger LOG = Log.getLogger();

  private final ClassPath     classPath;
  private final ReportOptions data;

  public DependencyBasedCoverageDatabase(final ClassPath classPath,
      final ReportOptions data) {
    this.data = data;
    this.classPath = classPath;
  }

  public Map<ClassGrouping, List<String>> mapCodeToTests(
      final Iterable<Class<?>> tests,
      final Map<String, ClassGrouping> groupedByOuterClass) throws IOException {

    final Map<ClassGrouping, List<String>> codeToTests = mapCodeToTests(
        convertClassesToStrings(tests), groupedByOuterClass);

    LOG.info("Dependency analysis finds tests for " + codeToTests.size()
        + " classes");

    return codeToTests;
  }

  private Collection<String> convertClassesToStrings(
      final Iterable<Class<?>> classes) {
    return FCollection.map(classes, Functions.classToName());
  }

  private Map<ClassGrouping, List<String>> mapCodeToTests(
      final Collection<String> tests,
      final Map<String, ClassGrouping> groupedByOuterClass) throws IOException {
    final Map<String, List<ClassGrouping>> testToCodeMap = mapTestsToCode(
        tests, groupedByOuterClass);

    final Map<ClassGrouping, List<String>> codeToTestsMap = new HashMap<ClassGrouping, List<String>>();

    for (final Entry<String, List<ClassGrouping>> each : testToCodeMap
        .entrySet()) {
      forEach(each.getValue(),
          mapCodeClassToTestClasses(each.getKey(), codeToTestsMap));

    }

    return codeToTestsMap;

  }

  private SideEffect1<ClassGrouping> mapCodeClassToTestClasses(
      final String test, final Map<ClassGrouping, List<String>> codeToTestsMap) {
    return new SideEffect1<ClassGrouping>() {
      public void apply(final ClassGrouping clazz) {

        List<String> testsForClass = codeToTestsMap.get(clazz);
        if (testsForClass == null) {
          testsForClass = new ArrayList<String>();
          codeToTestsMap.put(clazz, testsForClass);
        }
        testsForClass.add(test);
      }
    };
  }

  private Map<String, List<ClassGrouping>> mapTestsToCode(
      final Collection<String> tests,
      final Map<String, ClassGrouping> groupedByOuterClass) throws IOException {
    final DependencyExtractor analyser = new DependencyExtractor(
        new ClassPathByteArraySource(this.classPath),
        this.data.getDependencyAnalysisMaxDistance());
    final Map<String, List<ClassGrouping>> testToCodeMap = new HashMap<String, List<ClassGrouping>>();
    for (final String each : tests) {
      final Set<String> testReach = analyser
          .extractCallDependenciesForPackages(each,
              this.data.getClassesInScopeFilter());
      LOG.info(each + " reaches " + testReach.size() + " classes");

      final List<ClassGrouping> group = flatMap(testReach,
          jvmClassToGroup(groupedByOuterClass));
      if (group != null) {
        testToCodeMap.put(each, group);
      } else {
        LOG.warning("No group found for " + each);
      }

    }
    return testToCodeMap;
  }

  private F<String, Iterable<ClassGrouping>> jvmClassToGroup(
      final Map<String, ClassGrouping> groupedByOuterClass) {

    return new F<String, Iterable<ClassGrouping>>() {

      public Iterable<ClassGrouping> apply(final String a) {

        final String name = a.replace("/", ".");
        return Option.some(groupedByOuterClass.get(name));
      }

    };
  }

}
