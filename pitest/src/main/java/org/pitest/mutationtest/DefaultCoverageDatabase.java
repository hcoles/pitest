package org.pitest.mutationtest;

import static org.pitest.functional.FCollection.filter;
import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.FCollection.forEach;
import static org.pitest.functional.Prelude.and;
import static org.pitest.functional.Prelude.noSideEffect;
import static org.pitest.functional.Prelude.not;
import static org.pitest.functional.Prelude.printWith;
import static org.pitest.util.Functions.stringToClass;
import static org.pitest.util.TestInfo.isWithinATestClass;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
import org.pitest.functional.predicate.True;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.reflection.Reflection;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.MemoryEfficientHashMap;
import org.pitest.util.PortFinder;
import org.pitest.util.ProcessArgs;
import org.pitest.util.Unchecked;

public class DefaultCoverageDatabase implements CoverageDatabase {
  private final static Logger                              LOG             = Log
  .getLogger();

  private final Configuration                              initialConfig;
  private final JavaAgent                                  javaAgentFinder;
  private final ClassPath                                  classPath;
  private final ReportOptions                              data;

  private final Map<String, ClassInfo>                     nameToClassInfo = new MemoryEfficientHashMap<String, ClassInfo>();
  private final Map<String, Map<ClassLine, Set<TestInfo>>> classCoverage   = new MemoryEfficientHashMap<String, Map<ClassLine, Set<TestInfo>>>();
  private final Map<Description, Long>                     times           = new MemoryEfficientHashMap<Description, Long>();

  private List<Class<?>>                                   codeClasses;
  private Collection<ClassGrouping>                        groupedClasses;

  private boolean                                          allTestsGreen   = true;

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

    this.groupedClasses = groupByOuterClass(this.codeClasses);

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
    FCollection.mapTo(descriptions, descriptionToClass(),
        uniqueDiscoveredTestClasses);
    return uniqueDiscoveredTestClasses;
  }

  private static F<Description, Class<?>> descriptionToClass() {
    return new F<Description, Class<?>>() {

      public Class<?> apply(final Description a) {
        return a.getFirstTestClass();
      }

    };
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

  private void gatherCoverageData(final Collection<Class<?>> tests)
  throws IOException, InterruptedException {

    final List<TestUnit> filteredTests = extractRelevantTests(tests);

    final SideEffect1<CoverageResult> handler = resultProcessor();

    final PortFinder pf = PortFinder.INSTANCE;
    final int port = pf.getNextAvailablePort();

    final SlaveArguments sa = new SlaveArguments(
        convertToJVMClassFilter(this.data.getTargetClassesFilter()),
        this.data.isVerbose());

    final CoverageProcess process = new CoverageProcess(ProcessArgs
        .withClassPath(this.classPath).andJVMArgs(this.data.getJvmArgs())
        .andJavaAgentFinder(this.javaAgentFinder)
        .andStderr(printWith("stderr "))
        .andStdout(captureStandardOutIfVerbose()), sa, port, filteredTests,
        handler);

    process.start();
    process.waitToDie();
  }

  private SideEffect1<String> captureStandardOutIfVerbose() {
    if (this.data.isVerbose()) {
      return printWith("stdout ");
    } else {
      return noSideEffect(String.class);
    }
  }

  private List<TestUnit> extractRelevantTests(final Collection<Class<?>> tests) {
    final List<TestUnit> tus = Pitest.findTestUnitsForAllSuppliedClasses(
        this.initialConfig, new NullDiscoveryListener(),
        new UnGroupedStrategy(), tests.toArray(new Class<?>[tests.size()]));

    final List<TestUnit> tusWithinDistanceOfCodeClasses = filterTestsByDependencyAnalysis(tus);
    LOG.info("Dependency analysis reduced number of potential tests by "
        + (tus.size() - tusWithinDistanceOfCodeClasses.size()));
    return tusWithinDistanceOfCodeClasses;
  }

  private List<TestUnit> filterTestsByDependencyAnalysis(
      final List<TestUnit> tus) {
    final int maxDistance = this.data.getDependencyAnalysisMaxDistance();
    if (maxDistance < 0) {
      return tus;
    } else {
      return FCollection.filter(tus, isWithinReach(maxDistance));
    }
  }

  private F<TestUnit, Boolean> isWithinReach(final int maxDistance) {
    final DependencyExtractor analyser = new DependencyExtractor(
        new ClassPathByteArraySource(this.classPath), maxDistance);

    return new F<TestUnit, Boolean>() {
      private final Map<String, Boolean> cache = new HashMap<String, Boolean>();

      public Boolean apply(final TestUnit a) {
        final String each = a.getDescription().getFirstTestClass().getName();
        try {
          boolean inReach;
          if (this.cache.containsKey(each)) {
            inReach = this.cache.get(each);
          } else {
            inReach = !analyser.extractCallDependenciesForPackages(each,
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

        DefaultCoverageDatabase.this.times.put(cr.getTestUnitDescription(),
            cr.getExecutionTime());

        calculateClassCoverage(cr);

      }

    };
  }

  private void calculateClassCoverage(final CoverageResult each) {
    for (final ClassStatistics i : each.getCoverage()) {
      Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(i
          .getClassName());
      if (map == null) {
        map = new MemoryEfficientHashMap<ClassLine, Set<TestInfo>>();
        this.classCoverage.put(i.getClassName(), map);
      }
      mapTestsToClassLines(each.getTestUnitDescription(), i, map);

    }
  }

  private void mapTestsToClassLines(final Description description,
      final ClassStatistics i, final Map<ClassLine, Set<TestInfo>> map) {

    for (final int line : i.getUniqueVisitedLines()) {
      final ClassLine key = new ClassLine(i.getClassName(), line);
      Set<TestInfo> testsForLine = map.get(key);
      if (testsForLine == null) {
        testsForLine = new TreeSet<TestInfo>(new TestInfoNameComparator()); // inject
        // comparator
        // here
        map.put(key, testsForLine);
      }
      testsForLine.add(this.descriptionToTestInfo(description));

    }
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

  private Collection<ClassGrouping> groupByOuterClass(
      final Collection<Class<?>> classes) {
    final Map<String, ClassGrouping> group = new HashMap<String, ClassGrouping>();
    forEach(classes, addToMap(group, Reflection.isTopClass()));

    forEach(classes, addToParentGrouping(group));

    if (group.isEmpty()) {
      forEach(classes, addToMap(group, True.<Class<?>> all()));
    }

    return group.values();

  }

  private SideEffect1<Class<?>> addToMap(final Map<String, ClassGrouping> map,
      final Predicate<Class<?>> predicate) {
    return new SideEffect1<Class<?>>() {

      public void apply(final Class<?> clazz) {
        if (predicate.apply(clazz)) {
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
    @SuppressWarnings("unchecked")
    final FunctionalList<String> codeClassNames = FCollection
    .filter(this.codeClasses,
        and(not(Functions.isInnerClass()), not(Functions.isInterface())))
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

  public Collection<TestInfo> getTestForLineNumber(final ClassLine classLine) {
    return FCollection.flatMap(this.classCoverage.values(),
        flattenMap(classLine));
  }

  private F<Map<ClassLine, Set<TestInfo>>, Iterable<TestInfo>> flattenMap(
      final ClassLine classLine) {
    return new F<Map<ClassLine, Set<TestInfo>>, Iterable<TestInfo>>() {

      public Iterable<TestInfo> apply(final Map<ClassLine, Set<TestInfo>> a) {
        final Set<TestInfo> value = a.get(classLine);
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
    final Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(clazz
        .replace(".", "/"));
    if (map != null) {
      return map.size();
    } else {
      return 0;
    }

  }

  private TestInfo descriptionToTestInfo(final Description description) {
    final int time = DefaultCoverageDatabase.this.times.get(description)
    .intValue();

    return new TestInfo(description
        .getFirstTestClass().getName(), description.getQualifiedName(), time,
        description.getDirectTestees());
  }

  public Collection<ClassGrouping> getGroupedClasses() {
    return this.groupedClasses;
  }

  public Collection<TestInfo> getTestsForClass(final String clazz) {
    final Map<ClassLine, Set<TestInfo>> map = getTestsForJVMClassName(clazz);

    final Set<TestInfo> tis = new HashSet<TestInfo>();
    for (final Set<TestInfo> each : map.values()) {
      tis.addAll(each);
    }
    return tis;

  }

  private Map<ClassLine, Set<TestInfo>> getTestsForJVMClassName(
      final String clazz) {
    // Use any test that provided some coverage of the class
    // This fails to consider tests that only accessed a static variable
    // of the class in question as this does not register as coverage.

    Map<ClassLine, Set<TestInfo>> map = this.classCoverage.get(clazz);
    if (map == null) {
      map = new MemoryEfficientHashMap<ClassLine, Set<TestInfo>>();
    }
    return map;
  }

  public Collection<TestInfo> getTestsForClassLine(final ClassLine classLine) {
    return getTestsForJVMClassName(classLine.getJVMClassName()).get(classLine);
  }

}
