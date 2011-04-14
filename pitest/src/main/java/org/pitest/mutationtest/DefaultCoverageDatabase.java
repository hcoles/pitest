package org.pitest.mutationtest;

import static org.pitest.functional.Prelude.isEqualTo;
import static org.pitest.functional.Prelude.printWith;
import static org.pitest.util.Functions.classToName;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

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
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.mutationtest.instrument.CoverageSource;
import org.pitest.mutationtest.instrument.DefaultCoverageSource;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.MemoryEfficientHashMap;
import org.pitest.util.WrappingProcess;

public class DefaultCoverageDatabase implements CoverageDatabase {
  private final static Logger                                 LOG           = Log
                                                                                .getLogger();

  private final Configuration                                 initialConfig;
  private final JavaAgent                                     javaAgentFinder;
  private final ClassPath                                     classPath;
  private final ReportOptions                                 data;

  // required to choose sensible tests for mutations in static initializers
  private final DependencyBasedCoverageDatabase               dependencyInfo;

  // private FunctionalList<CoverageResult> coverage;
  private final Map<String, Map<ClassLine, Set<Description>>> classCoverage = new MemoryEfficientHashMap<String, Map<ClassLine, Set<Description>>>();
  private final Map<Description, Long>                        times         = new MemoryEfficientHashMap<Description, Long>();

  private boolean                                             allTestsGreen = true;

  public DefaultCoverageDatabase(final Configuration initialConfig,
      final ClassPath classPath, final JavaAgent javaAgentFinder,
      final ReportOptions data) {
    this.classPath = classPath;
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
    this.initialConfig = initialConfig;
    this.dependencyInfo = new DependencyBasedCoverageDatabase(classPath, data);
  }

  public Map<ClassGrouping, List<String>> mapCodeToTests(
      final Map<String, ClassGrouping> groupedByOuterClass) throws IOException {

    final Set<Class<?>> uniqueDiscoveredTestClasses = new HashSet<Class<?>>();

    FCollection.flatMap(this.times.keySet(), Prelude.id(Description.class),
        uniqueDiscoveredTestClasses);

    // can't use coverage data if we are mutating static initializers
    // as only first test to use a class will appear to cover this code
    return this.dependencyInfo.mapCodeToTests(uniqueDiscoveredTestClasses,
        groupedByOuterClass);

  }

  private void gatherCoverageData(final Collection<Class<?>> tests)
      throws IOException, InterruptedException {

    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        this.initialConfig, new NullDiscoveryListener(),
        new UnGroupedStrategy(), Option.<TestFilter> none(),
        tests.toArray(new Class<?>[tests.size()]));

    final SideEffect1<CoverageResult> handler = resultProcessor();

    final int port = 8187;

    // final CoverageReceiverThread crt = new CoverageReceiverThread(port, tus,
    // handler);
    // crt.start();

    final SlaveArguments sa = new SlaveArguments(tus, System.getProperties(),
        convertToJVMClassFilter(this.data.getTargetClassesFilter()), port);
    final CoverageProcess process = new CoverageProcess(WrappingProcess.Args
        .withClassPath(this.classPath).andJVMArgs(this.data.getJvmArgs())
        .andJavaAgentFinder(this.javaAgentFinder)
        .andStderr(printWith("SLAVE : ")), sa, port, tus, handler);

    process.waitToDie();
    // crt.waitToFinish();

    process.cleanUp();
  }

  private Predicate<String> convertToJVMClassFilter(
      final Predicate<String> child) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return child.apply(a.replace("/", "."));
      }

    };
  }

  public boolean initialise(final Collection<Class<?>> tests) {
    try {
      final long t0 = System.currentTimeMillis();

      gatherCoverageData(tests);

      final long time = (System.currentTimeMillis() - t0) / 1000;

      LOG.info("Calculated coverage in " + time + " seconds.");

    } catch (final IOException e) {
      e.printStackTrace();
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }

    return this.allTestsGreen;

  }

  private SideEffect1<CoverageResult> resultProcessor() {
    return new SideEffect1<CoverageResult>() {

      public void apply(final CoverageResult cr) {

        if (!cr.isGreenTest()) {
          DefaultCoverageDatabase.this.allTestsGreen = false;
          LOG.warning(cr.getTestUnitDescription()
              + " did not pass without mutation.");
        }
        calculateClassCoverage(cr);

        DefaultCoverageDatabase.this.times.put(cr.getTestUnitDescription(),
            cr.getExecutionTime());

      }

    };
  }

  private void calculateClassCoverage(final CoverageResult each) {
    for (final ClassStatistics i : each.getCoverage()) {
      Map<ClassLine, Set<Description>> map = this.classCoverage.get(i
          .getClassName());
      if (map == null) {
        map = new MemoryEfficientHashMap<ClassLine, Set<Description>>();
        this.classCoverage.put(i.getClassName(), map);
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

    return new DefaultCoverageSource(tests, this.initialConfig,
        getTimings(tests), coverageByTestUnit(code));
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

  private Map<String, Long> getTimings(final List<String> tests) {
    final Map<String, Long> timings = new MemoryEfficientHashMap<String, Long>();

    FCollection.filter(this.times.keySet(), isForTests(tests)).forEach(
        addToMap(timings));

    return timings;

  }

  private SideEffect1<Description> addToMap(final Map<String, Long> timings) {
    return new SideEffect1<Description>() {
      public void apply(final Description result) {
        timings.put(result.toString(),
            DefaultCoverageDatabase.this.times.get(result));
      }

    };
  }

  private F<Description, Boolean> isForTests(final List<String> tests) {
    return new F<Description, Boolean>() {

      public Boolean apply(final Description a) {
        return FCollection.contains(tests, oneOf(a.getTestClasses()));
      }

    };
  }

  private F<String, Boolean> oneOf(final Collection<Class<?>> testClasses) {
    return new F<String, Boolean>() {

      public Boolean apply(final String a) {
        return FCollection.map(testClasses, classToName()).contains(
            isEqualTo(a));
      }

    };
  }

}
