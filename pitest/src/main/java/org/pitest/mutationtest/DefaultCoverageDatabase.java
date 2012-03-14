package org.pitest.mutationtest;

import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.FCollection.forEach;
import static org.pitest.functional.Prelude.and;
import static org.pitest.functional.Prelude.noSideEffect;
import static org.pitest.functional.Prelude.not;
import static org.pitest.functional.Prelude.printWith;

import java.io.IOException;
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
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.ClassName;
import org.pitest.classinfo.Repository;
import org.pitest.classinfo.TestToClassMapper;
import org.pitest.coverage.ClassStatistics;
import org.pitest.coverage.domain.TestInfo;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.execute.CoverageProcess;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.coverage.execute.LaunchOptions;
import org.pitest.extension.Configuration;
import org.pitest.functional.F;
import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.True;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.mutationtest.instrument.ClassLine;
import org.pitest.util.JavaAgent;
import org.pitest.util.Log;
import org.pitest.util.MemoryEfficientHashMap;
import org.pitest.util.PortFinder;
import org.pitest.util.ProcessArgs;

public class DefaultCoverageDatabase implements CoverageDatabase {
  private final static Logger                              LOG           = Log
                                                                             .getLogger();

  private final CoverageOptions                            coverageOptions;
  private final LaunchOptions                              launchOptions;
  private final MutationClassPaths                         classPath;

  private final Repository                                 classRepository;
  private final Map<String, Map<ClassLine, Set<TestInfo>>> classCoverage = new MemoryEfficientHashMap<String, Map<ClassLine, Set<TestInfo>>>();
  private final Map<Description, Long>                     times         = new MemoryEfficientHashMap<Description, Long>();

  private final List<ClassInfo>                            codeClasses;
  private Collection<ClassGrouping>                        groupedClasses;

  private boolean                                          allTestsGreen = true;
  private final TestToClassMapper                          testClassMapper;

  private final Timings                                    timings;

  public DefaultCoverageDatabase(final CoverageOptions coverageOptions,
      final LaunchOptions launchOptions, final MutationClassPaths classPath,
      final Timings timings) {
    this.coverageOptions = coverageOptions;
    this.classPath = classPath;
    this.launchOptions = launchOptions;
    this.classRepository = new Repository(new ClassPathByteArraySource(
        classPath.getClassPath()));
    this.testClassMapper = new TestToClassMapper(this.classRepository);
    this.timings = timings;
    this.codeClasses = FCollection.flatMap(this.classPath.code(),
        nameToClassInfo()).filter(not(isWithinATestClass()));
  }

  public boolean initialise() {

    this.timings.registerStart(Timings.Stage.SCAN_CLASS_PATH);
    @SuppressWarnings("unchecked")
    final FunctionalCollection<ClassInfo> directlySuppliedTestsAndSuites = flatMap(
        this.classPath.test(), nameToClassInfo()).filter(
        and(isWithinATestClass(), not(ClassInfo.matchIfAbstract())));

    LOG.info("Found " + directlySuppliedTestsAndSuites.size()
        + " classes that might define tests");
    this.timings.registerEnd(Timings.Stage.SCAN_CLASS_PATH);

    this.timings.registerStart(Timings.Stage.COVERAGE);
    calculateCoverage(directlySuppliedTestsAndSuites);
    this.timings.registerEnd(Timings.Stage.COVERAGE);

    this.groupedClasses = groupByOuterClass(this.codeClasses);

    verifyBuildSuitableForMutationTesting();

    return this.allTestsGreen;

  }

  private void verifyBuildSuitableForMutationTesting() {
    if (!this.allTestsGreen) {
      throw new PitHelpError(Help.FAILING_TESTS);
    }
  }

  private F<ClassInfo, Boolean> isWithinATestClass() {
    return new F<ClassInfo, Boolean>() {

      public Boolean apply(final ClassInfo a) {
        return DefaultCoverageDatabase.this.coverageOptions.getPitConfig()
            .testClassIdentifier().isATestClass(a);
      }

    };

  }

  private F<String, Option<ClassInfo>> nameToClassInfo() {
    return new F<String, Option<ClassInfo>>() {

      public Option<ClassInfo> apply(final String a) {
        return DefaultCoverageDatabase.this.classRepository.fetchClass(a);
      }

    };
  }

  private void calculateCoverage(final FunctionalCollection<ClassInfo> tests) {
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

  private void gatherCoverageData(final Collection<ClassInfo> tests)
      throws IOException, InterruptedException {

    final List<String> filteredTests = FCollection
        .map(tests, classInfoToName());

    final SideEffect1<CoverageResult> handler = resultProcessor();

    final PortFinder pf = PortFinder.INSTANCE;
    final int port = pf.getNextAvailablePort();

    final CoverageProcess process = new CoverageProcess(ProcessArgs
        .withClassPath(this.classPath.getClassPath())
        .andJVMArgs(this.launchOptions.getChildJVMArgs())
        .andJavaAgentFinder(this.launchOptions.getJavaAgentFinder())
        .andStderr(printWith("stderr "))
        .andStdout(captureStandardOutIfVerbose()), this.coverageOptions, port,
        filteredTests, handler);

    process.start();
    process.waitToDie();
  }

  private static F<ClassInfo, String> classInfoToName() {
    return new F<ClassInfo, String>() {
      public String apply(final ClassInfo a) {
        return a.getName().asInternalName();
      }

    };
  }

  private SideEffect1<String> captureStandardOutIfVerbose() {
    if (this.coverageOptions.isVerbose()) {
      return printWith("stdout ");
    } else {
      return noSideEffect(String.class);
    }
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

  private Collection<ClassGrouping> groupByOuterClass(
      final Collection<ClassInfo> classes) {
    final Map<ClassName, ClassGrouping> group = new HashMap<ClassName, ClassGrouping>();
    forEach(classes, addToMap(group, ClassInfo.matchIfTopLevelClass()));
    forEach(classes, addToParentGrouping(group));

    if (group.isEmpty()) {
      forEach(classes, addToMap(group, True.<ClassInfo> all()));
    }

    return group.values();

  }

  private SideEffect1<ClassInfo> addToMap(
      final Map<ClassName, ClassGrouping> map,
      final F<ClassInfo, Boolean> predicate) {
    return new SideEffect1<ClassInfo>() {

      public void apply(final ClassInfo clazz) {
        if (predicate.apply(clazz)) {
          map.put(clazz.getName(), new ClassGrouping(clazz.getName()
              .asInternalName(), Collections.<String> emptyList()));
        }
      }

    };
  }

  private SideEffect1<ClassInfo> addToParentGrouping(
      final Map<ClassName, ClassGrouping> map) {
    return new SideEffect1<ClassInfo>() {

      public void apply(final ClassInfo a) {
        final Option<ClassInfo> parent = a.getOuterClass();
        if (parent.hasSome()) {
          final ClassGrouping grouping = map.get(parent.value().getName());
          if (grouping != null) {
            grouping.addChild(a.getName().asInternalName());
          }
        }

      }

    };
  }

  public Collection<String> getParentClassesWithoutATest() {
    @SuppressWarnings("unchecked")
    final FunctionalList<String> codeClassNames = FCollection
        .filter(
            this.codeClasses,
            and(ClassInfo.matchIfTopLevelClass(),
                not(ClassInfo.matchIfInterface()))).map(classInfoToName());
    return codeClassNames.filter(Prelude.not(hasTest()));
  }

  private F<String, Boolean> hasTest() {
    return new F<String, Boolean>() {

      public Boolean apply(final String a) {
        return DefaultCoverageDatabase.this.classCoverage.containsKey(a
            .replace('.', '/'));
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
      cis.add(this.classRepository.fetchClass(each).value());
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

    final Option<ClassName> testee = this.testClassMapper
        .findTestee(description.getFirstTestClass());

    return new TestInfo(description.getFirstTestClass(),
        description.getQualifiedName(), time, testee);

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

  public Configuration getConfiguration() {
    return this.coverageOptions.getPitConfig();
  }

  public JavaAgent getJavaAgent() {
    return this.launchOptions.getJavaAgentFinder();
  }

  public Collection<ClassInfo> getCodeClasses() {
    return this.codeClasses;
  }

}
