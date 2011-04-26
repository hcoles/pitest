package org.pitest.mutationtest;

import static org.pitest.functional.FCollection.filter;
import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.FCollection.forEach;
import static org.pitest.functional.Prelude.and;
import static org.pitest.functional.Prelude.not;
import static org.pitest.functional.Prelude.printWith;
import static org.pitest.util.Functions.stringToClass;
import static org.pitest.util.TestInfo.isWithinATestClass;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassInfoVisitor;
import org.pitest.coverage.ClassStatistics;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.coverage.execute.CoverageProcess;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.coverage.execute.SlaveArguments;
import org.pitest.dependency.DependencyExtractor;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.reflection.Reflection;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.MemoryEfficientHashMap;
import org.pitest.util.Unchecked;
import org.pitest.util.WrappingProcess;

public class DefaultCoverageDatabase implements CoverageDatabase {
  private final static Logger                                 LOG             = Log
                                                                                  .getLogger();

  private final Configuration                                 initialConfig;
  private final JavaAgent                                     javaAgentFinder;
  private final ClassPath                                     classPath;
  private final ReportOptions                                 data;

  private final Map<String, ClassInfo>                        nameToClassInfo = new MemoryEfficientHashMap<String, ClassInfo>();

  private final Map<String, Map<ClassLine, Set<Description>>> classCoverage   = new MemoryEfficientHashMap<String, Map<ClassLine, Set<Description>>>();

  private final Map<Description, Long>                        times           = new MemoryEfficientHashMap<Description, Long>();

  private List<Class<?>>                                      codeClasses;
  private Map<String, ClassGrouping>                          groupedByOuterClass;
  private boolean                                             allTestsGreen   = true;

  public DefaultCoverageDatabase(final Configuration initialConfig,
      final ClassPath classPath, final JavaAgent javaAgentFinder,
      final ReportOptions data) {
    this.classPath = classPath;
    this.data = data;
    this.javaAgentFinder = javaAgentFinder;
    this.initialConfig = initialConfig;
  }

  public boolean initialise() {

    final Collection<Class<?>> completeClassPath = flatMap(completeClassPath(),
        stringToClass());

    @SuppressWarnings("unchecked")
    final FunctionalCollection<Class<?>> directlySuppliedTestsAndSuites = flatMap(
        completeClassPathForTests(), stringToClass()).filter(
        and(isWithinATestClass(), isNotAbstract()));

    calculateCoverage(directlySuppliedTestsAndSuites);

    final Set<Class<?>> uniqueDiscoveredTestClasses = gatherUniqueClassesFromDescriptions(this.times
        .keySet());

    this.codeClasses = filter(
        extractCodeClasses(completeClassPath, uniqueDiscoveredTestClasses),
        convertStringToClassFilter(this.data.getTargetClassesFilter()));

    analyseClasses();

    this.groupedByOuterClass = groupByOuterClass(this.codeClasses);

    return this.allTestsGreen;

  }

  private void analyseClasses() {
    for (final ClassInfo each : namesToClassInfo(this.codeClasses)) {
      this.nameToClassInfo.put(each.getName(), each);
    }
  }

  private Collection<ClassInfo> namesToClassInfo(
      final Collection<Class<?>> classes) {
    return FCollection.flatMap(classes, nameToClassInfo());
  }

  private F<Class<?>, Option<ClassInfo>> nameToClassInfo() {
    return new F<Class<?>, Option<ClassInfo>>() {

      public Option<ClassInfo> apply(final Class<?> a) {
        final ClassPathByteArraySource source = new ClassPathByteArraySource(
            DefaultCoverageDatabase.this.classPath);
        final Option<byte[]> bytes = source.apply(a.getName());
        if (bytes.hasSome()) {
          return Option.some(ClassInfoVisitor.getClassInfo(a.getName(),
              bytes.value()));
        } else {
          return Option.none();
        }
      }

    };
  }

  private Set<Class<?>> gatherUniqueClassesFromDescriptions(
      final Iterable<Description> descriptions) {
    final Set<Class<?>> uniqueDiscoveredTestClasses = new HashSet<Class<?>>();
    FCollection.flatMap(descriptions, Prelude.id(Description.class),
        uniqueDiscoveredTestClasses);
    return uniqueDiscoveredTestClasses;
  }

  private void calculateCoverage(final FunctionalCollection<Class<?>> tests) {
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
  }

  public Map<ClassGrouping, List<String>> mapCodeToTests() throws IOException {

    final Map<ClassGrouping, List<String>> groupsToTests = new MemoryEfficientHashMap<ClassGrouping, List<String>>();

    for (final ClassGrouping each : this.groupedByOuterClass.values()) {
      final Map<ClassLine, Set<Description>> coverage = this
          .coverageByTestUnit(each);
      final Set<Class<?>> uniqueDiscoveredTestClasses = new HashSet<Class<?>>();
      FCollection.flatMap(coverage.values(), flattenList(),
          uniqueDiscoveredTestClasses);
      groupsToTests
          .put(
              each,
              FCollection.map(uniqueDiscoveredTestClasses,
                  Functions.classToName()));
    }

    return groupsToTests;

  }

  private F<Set<Description>, Iterable<Class<?>>> flattenList() {
    return new F<Set<Description>, Iterable<Class<?>>>() {

      public Iterable<Class<?>> apply(final Set<Description> as) {
        return FCollection.flatMap(as, Prelude.id(Description.class));
      }

    };
  }

  private void gatherCoverageData(final Collection<Class<?>> tests)
      throws IOException, InterruptedException {

    final List<TestUnit> filteredTests = extractRelevantTests(tests);

    final SideEffect1<CoverageResult> handler = resultProcessor();

    final int port = 8187;

    final SlaveArguments sa = new SlaveArguments(filteredTests,
        System.getProperties(),
        convertToJVMClassFilter(this.data.getTargetClassesFilter()), port);
    final CoverageProcess process = new CoverageProcess(WrappingProcess.Args
        .withClassPath(this.classPath).andJVMArgs(this.data.getJvmArgs())
        .andJavaAgentFinder(this.javaAgentFinder)
        .andStderr(printWith("SLAVE : ")), sa, port, filteredTests, handler);

    process.waitToDie();

    process.cleanUp();
  }

  private List<TestUnit> extractRelevantTests(final Collection<Class<?>> tests) {
    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        this.initialConfig, new NullDiscoveryListener(),
        new UnGroupedStrategy(), Option.<TestFilter> none(),
        tests.toArray(new Class<?>[tests.size()]));

    final List<TestUnit> tusWithinDistanceOfCodeClasses = filterTestsByDependencyAnalysis(tus);
    LOG.info("Dependency analysis reduced number of potential tests by "
        + (tus.size() - tusWithinDistanceOfCodeClasses.size()));
    return tusWithinDistanceOfCodeClasses;
  }

  private List<TestUnit> filterTestsByDependencyAnalysis(
      final List<TestUnit> tus) {
    final int maxDistance = this.data.getDependencyAnalysisMaxDistance();
    if (maxDistance <= 0) {
      return tus;
    } else {
      return FCollection.filter(tus, isWithinReach(maxDistance));
    }
  }

  private F<TestUnit, Boolean> isWithinReach(final int maxDistance) {
    final DependencyExtractor analyser = new DependencyExtractor(
        new ClassPathByteArraySource(this.classPath), maxDistance);

    return new F<TestUnit, Boolean>() {
      private final Map<Class<?>, Boolean> cache = new HashMap<Class<?>, Boolean>();

      public Boolean apply(final TestUnit a) {
        for (final Class<?> each : a.getDescription().getTestClasses()) {
          try {
            boolean inReach;
            if (this.cache.containsKey(each)) {
              inReach = this.cache.get(each);
            } else {
              inReach = !analyser.extractCallDependenciesForPackages(
                  each.getName(),
                  DefaultCoverageDatabase.this.data.getTargetClassesFilter())
                  .isEmpty();
              this.cache.put(each, inReach);
            }

            if (inReach) {
              return true;
            }
          } catch (final IOException e) {
            throw Unchecked.translateCheckedException(e);
          }
        }
        return false;
      }

    };
  }

  private Predicate<String> convertToJVMClassFilter(
      final Predicate<String> child) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return child.apply(a.replace("/", "."));
      }

    };
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
      mapTestsToClassLines(each.getTestUnitDescription(), i, map);

    }
  }

  private void mapTestsToClassLines(final Description description,
      final ClassStatistics i, final Map<ClassLine, Set<Description>> map) {

    for (final int line : i.getUniqueVisitedLines()) {
      final ClassLine key = new ClassLine(i.getClassName(), line);
      Set<Description> testsForLine = map.get(key);
      if (testsForLine == null) {
        testsForLine = new TreeSet<Description>(
            new StringBasedDescriptionComparator()); // inject comparator here
        map.put(key, testsForLine);
      }
      testsForLine.add(description);

    }
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
    return lineToTests;
  }

  private Iterable<String> completeClassPathForTests() {
    return FCollection.filter(completeClassPath(),
        this.data.getTargetTestsFilter());
  }

  private Collection<String> completeClassPath() {
    return this.classPath.getLocalDirectoryComponent().findClasses(
        this.data.getClassesInScopeFilter());
  }

  private Predicate<Class<?>> isNotAbstract() {
    return new Predicate<Class<?>>() {

      public Boolean apply(final Class<?> a) {
        return !a.isInterface() && !Modifier.isAbstract(a.getModifiers());
      }

    };
  }

  private List<Class<?>> extractCodeClasses(final Collection<Class<?>> targets,
      final Collection<Class<?>> tests) {
    final List<Class<?>> cs = new ArrayList<Class<?>>();
    cs.addAll(targets);
    cs.removeAll(tests);
    return cs;
  }

  private F<Class<?>, Boolean> convertStringToClassFilter(
      final Predicate<String> predicate) {
    return new F<Class<?>, Boolean>() {

      public Boolean apply(final Class<?> a) {
        return predicate.apply(a.getName());
      }

    };
  }

  private Map<String, ClassGrouping> groupByOuterClass(
      final Collection<Class<?>> classes) {
    final Map<String, ClassGrouping> group = new HashMap<String, ClassGrouping>();
    forEach(classes, addToMapIfTopLevelClass(group));

    forEach(classes, addToParentGrouping(group));

    return group;

  }

  private SideEffect1<Class<?>> addToMapIfTopLevelClass(
      final Map<String, ClassGrouping> map) {
    return new SideEffect1<Class<?>>() {

      public void apply(final Class<?> clazz) {
        if (Reflection.isTopClass(clazz)) {
          map.put(clazz.getName(), new ClassGrouping(clazz.getName(),
              Collections.<String> emptyList()));
        }
      }

    };
  }

  private SideEffect1<Class<?>> addToParentGrouping(
      final Map<String, ClassGrouping> map) {
    return new SideEffect1<Class<?>>() {

      public void apply(final Class<?> a) {
        final Option<Class<?>> parent = Reflection.getParentClass(a);
        if (parent.hasSome()) {
          final ClassGrouping grouping = map.get(parent.value().getName());
          if (grouping != null) {
            grouping.addChild(a);
          }
        }

      }

    };
  }

  public Collection<String> getParentClassesWithoutATest() {
    final FunctionalList<String> codeClassNames = FCollection
        .filter(this.codeClasses, not(Functions.isInnerClass()))
        .map(Functions.classToName()).map(Functions.classNameToJVMClassName());
    return codeClassNames.filter(Prelude.not(hasTest()));
  }

  private F<String, Boolean> hasTest() {
    return new F<String, Boolean>() {

      public Boolean apply(final String a) {
        return DefaultCoverageDatabase.this.classCoverage.containsKey(a);
      }

    };
  }

  public Collection<Description> getTestForLineNumber(final ClassLine classLine) {
    return FCollection.flatMap(this.classCoverage.values(),
        flattenMap(classLine));
  }

  private F<Map<ClassLine, Set<Description>>, Iterable<Description>> flattenMap(
      final ClassLine classLine) {
    return new F<Map<ClassLine, Set<Description>>, Iterable<Description>>() {

      public Iterable<Description> apply(
          final Map<ClassLine, Set<Description>> a) {
        final Set<Description> value = a.get(classLine);
        if (value != null) {
          return value;
        }
        return Collections.emptySet();
      }

    };
  }

  public Collection<ClassInfo> getClassInfo(final Collection<String> classes) {
    final Collection<ClassInfo> cis = new ArrayList<ClassInfo>();
    for (final String each : classes) {
      cis.add(this.nameToClassInfo.get(each));
    }
    return cis;
  }

  public int getNumberOfCoveredLines(final Collection<String> mutatedClass) {
    return FCollection.fold(numberCoveredLines(), 0, mutatedClass);
  }

  private F2<Integer, String, Integer> numberCoveredLines() {
    return new F2<Integer, String, Integer>() {

      public Integer apply(final Integer a, final String clazz) {
        return a + getNumberOfCoveredLines(clazz);
      }

    };
  }

  public int getNumberOfCoveredLines(final String clazz) {
    final Map<ClassLine, Set<Description>> map = this.classCoverage.get(clazz
        .replace(".", "/"));
    if (map != null) {
      return map.size();
    } else {
      return 0;
    }

  }

  public Collection<TestInfo> getTestsForMutant(final MutationDetails mutation) {

    Map<ClassLine, Set<Description>> map = this.classCoverage.get(mutation
        .getJVMClassName());
    if (map == null) {
      map = new MemoryEfficientHashMap<ClassLine, Set<Description>>();
    }

    Collection<Description> ds = null;
    if (!mutation.isInStaticInitializer()) {
      ds = map.get(mutation.getClassLine());
    } else {

      // Use any test that provided some coverage of the class
      // This fails to consider tests that only accessed a static variable
      // of the class in question as this does not register as coverage.

      LOG.warning("Using untargeted tests");
      ds = new HashSet<Description>();
      for (final Set<Description> each : map.values()) {
        ds.addAll(each);
      }

    }

    final FunctionalList<TestInfo> tis = FCollection.map(ds,
        descriptionToTestInfo());
    Collections.sort(tis, timeComparator());
    return tis;
  }

  private Comparator<TestInfo> timeComparator() {
    return new Comparator<TestInfo>() {

      public int compare(final TestInfo arg0, final TestInfo arg1) {
        final Long t0 = arg0.getTime();
        final Long t1 = arg1.getTime();
        return t0.compareTo(t1);
      }

    };
  }

  private F<Description, TestInfo> descriptionToTestInfo() {
    return new F<Description, TestInfo>() {

      public TestInfo apply(final Description a) {
        final long time = DefaultCoverageDatabase.this.times.get(a);
        return new TestInfo(a.toString(), time);
      }

    };
  }

}
