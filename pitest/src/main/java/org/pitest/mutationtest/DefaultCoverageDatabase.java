package org.pitest.mutationtest;

import static org.pitest.functional.Prelude.printWith;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.coverage.ClassStatistics;
import org.pitest.coverage.execute.CoverageProcess;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.coverage.execute.SlaveArguments;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.FunctionalCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.mutationtest.instrument.CoverageSource;
import org.pitest.mutationtest.instrument.DefaultCoverageSource;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;
import org.pitest.util.MemoryEfficientHashMap;
import org.pitest.util.WrappingProcess;

public class DefaultCoverageDatabase implements CoverageDatabase {

  private final Configuration                                 initialConfig;
  private final JavaAgent                                     javaAgentFinder;
  private final ClassPath                                     classPath;
  private final ReportOptions                                 data;

  private FunctionalList<CoverageResult>                      coverage;
  private final Map<String, Map<ClassLine, Set<Description>>> classCoverage = new MemoryEfficientHashMap<String, Map<ClassLine, Set<Description>>>();

  public DefaultCoverageDatabase(final Configuration initialConfig,
      final ClassPath classPath, final JavaAgent javaAgentFinder,
      final ReportOptions data) {
    this.classPath = classPath;
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
    this.initialConfig = initialConfig;
  }

  public Map<ClassGrouping, List<String>> mapCodeToTests(
      final FunctionalCollection<Class<?>> tests,
      final Map<String, ClassGrouping> groupedByOuterClass) throws IOException {

    final List<String> testsAsStrings = tests.map(Functions.classToName());

    // FIXME will use all tests for a static initializer
    // so must filter here
    final Map<ClassGrouping, List<String>> codeToTests = new HashMap<ClassGrouping, List<String>>();
    for (final ClassGrouping each : groupedByOuterClass.values()) {
      codeToTests.put(each, testsAsStrings);
    }

    return codeToTests;
  }

  private FunctionalList<CoverageResult> gatherCoverageData(
      final Collection<Class<?>> tests) throws IOException,
      InterruptedException {

    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        this.initialConfig, new NullDiscoveryListener(),
        new UnGroupedStrategy(), Option.<TestFilter> none(),
        tests.toArray(new Class<?>[tests.size()]));

    final SlaveArguments sa = new SlaveArguments(tus, System.getProperties(),
        convertToJVMClassFilter(this.data.getTargetClassesFilter()));
    final CoverageProcess process = new CoverageProcess(WrappingProcess.Args
        .withClassPath(this.classPath).andJVMArgs(this.data.getJvmArgs())
        .andJavaAgentFinder(this.javaAgentFinder)
        .andStderr(printWith("SLAVE : ")), sa);
    process.waitToDie();
    return process.results();

  }

  private Predicate<String> convertToJVMClassFilter(
      final Predicate<String> child) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return child.apply(a.replace("/", "."));
      }

    };
  }

  public void initialise(final FunctionalCollection<Class<?>> tests) {
    try {
      this.coverage = gatherCoverageData(tests);

      for (final CoverageResult each : this.coverage) {
        // System.out.println(each);
        calculateClassCoverage(each);

      }

      // coverage.forEach(e);

    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } catch (final InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

  }

  private void calculateClassCoverage(final CoverageResult each) {
    for (final ClassStatistics i : each.getCoverage()) {
      Map<ClassLine, Set<Description>> map = this.classCoverage.get(i
          .getClassName());
      if (map == null) {
        map = new MemoryEfficientHashMap<ClassLine, Set<Description>>();
        // System.out.println("2 -----> Got coverage for " + i.getClassName());
        this.classCoverage.put(i.getClassName(), map);
      } else {
        // System.out.println("Already have map for " + i.getClassName());

      }
      mapTestsToClassLines(each, i, map);

    }
  }

  private void mapTestsToClassLines(final CoverageResult each,
      final ClassStatistics i, final Map<ClassLine, Set<Description>> map) {
    for (final int line : i.getUniqueVisitedLines()) {
      final ClassLine key = new ClassLine(i.getClassName(), line);
      Set<Description> testsForLine = map.get(key);
      if (testsForLine == null) {
        testsForLine = new TreeSet<Description>(
            new StringBasedDescriptionComparator()); // inject comparator here
        map.put(key, testsForLine);
      }
      testsForLine.add(each.getTestUnitDescription());

    }
  }

  public CoverageSource getCoverage(final ClassGrouping code,
      final List<String> tests) {

    return new DefaultCoverageSource(tests, this.initialConfig, getTimings(),
        coverageByTestUnit(code));
  }

  private Map<ClassLine, Set<Description>> coverageByTestUnit(
      final ClassGrouping code) {
    final Map<ClassLine, Set<Description>> lineToTests = new MemoryEfficientHashMap<ClassLine, Set<Description>>();

    for (final String each : code) {
      final Map<ClassLine, Set<Description>> tests = this.classCoverage
          .get(each.replace(".", "/"));
      if (tests != null) {
        for (final Entry<ClassLine, Set<Description>> entry : tests.entrySet()) {
          lineToTests.put(entry.getKey(), entry.getValue());
        }
      }

    }

    // for (CoverageResult each : this.coverage) {
    // Set<Description> tests = lineToTests.get(each.);
    // TODO filter
    // / coverageToDescription.put(each.getTestUnitDescription(), value);
    // }

    return lineToTests;
  }

  private Map<String, Long> getTimings() {
    final Map<String, Long> timings = new MemoryEfficientHashMap<String, Long>();

    for (final CoverageResult each : this.coverage) {
      // TODO filter timings
      timings.put(each.getTestUnitDescription().toString(),
          each.getExecutionTime());
    }

    return timings;

  }

}
