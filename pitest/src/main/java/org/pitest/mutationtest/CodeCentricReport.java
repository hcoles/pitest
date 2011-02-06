/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */
package org.pitest.mutationtest;

import static org.pitest.functional.FCollection.filter;
import static org.pitest.functional.FCollection.flatMap;
import static org.pitest.functional.FCollection.forEach;
import static org.pitest.functional.FCollection.map;
import static org.pitest.functional.Prelude.id;
import static org.pitest.util.Functions.classToName;
import static org.pitest.util.Functions.jvmClassToClassName;
import static org.pitest.util.Functions.stringToClass;
import static org.pitest.util.TestInfo.isWithinATestClass;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.dependency.DependencyExtractor;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.ConsoleResultListener;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.Prelude;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.internal.ClassPathByteArraySource;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.instrument.InstrumentedMutationTestUnit;
import org.pitest.mutationtest.report.MutationHtmlReportListener;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;
import org.pitest.mutationtest.report.SmartSourceLocator;
import org.pitest.reflection.Reflection;
import org.pitest.util.Functions;
import org.pitest.util.JavaAgent;

public class CodeCentricReport extends MutationCoverageReport {

  private final JavaAgent javaAgentFinder;
  private final boolean   nonLocalClassPath;

  public CodeCentricReport(final ReportOptions data,
      final JavaAgent javaAgentFinder, final boolean nonLocalClassPath) {
    super(data);
    this.javaAgentFinder = javaAgentFinder;
    this.nonLocalClassPath = nonLocalClassPath;
  }

  @Override
  public void runReport() throws IOException {

    long t0 = System.currentTimeMillis();

    final Collection<Class<?>> completeClassPath = flatMap(getClassPath()
        .findClasses(this.data.getClassesInScopeFilter()), stringToClass());

    final Collection<Class<?>> tests = filter(completeClassPath,
        isWithinATestClass());

    final List<Class<?>> codeClasses = filter(
        extractCodeClasses(completeClassPath, tests),
        convertStringToClassFilter(this.data.getTargetClassesFilter()));

    final Map<String, ClassGrouping> groupedByOuterClass = groupByOuterClass(codeClasses);

    final Map<ClassGrouping, List<String>> codeToTests = mapCodeToTests(
        convertClassesToStrings(tests), groupedByOuterClass);

    System.out.println("Dependency analysis finds tests for "
        + codeToTests.size() + " classes");
    FCollection
        .forEach(codeToTests.keySet(), Prelude.printWith("Has Test =  "));

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final MutationHtmlReportListener mutationReportListener = new MutationHtmlReportListener(
        this.data.getReportDir(), new SmartSourceLocator(
            this.data.getSourceDirs()));
    staticConfig.addTestListener(mutationReportListener);
    staticConfig.addTestListener(new ConsoleResultListener());
    final ConcreteConfiguration initialConfig = new ConcreteConfiguration(
        new JUnitCompatibleConfiguration());

    reportFailureForClassesWithoutTests(
        classesWithoutATest(codeClasses, codeToTests), mutationReportListener);

    final List<TestUnit> tus = createMutationTestUnits(codeToTests,
        initialConfig);

    System.out.println("Created  " + tus.size() + " mutation test units");

    final Pitest pit = new Pitest(staticConfig, initialConfig);
    pit.run(new UnContainer(), tus);

    System.out.println("Completed in " + timeSpan(t0) + ".  Tested "
        + codeToTests.size() + " classes.");

  }

  private String timeSpan(long t0) {
    return "" + ((System.currentTimeMillis() - t0) / 1000) + " seconds";
  }

  private F<Class<?>, Boolean> convertStringToClassFilter(
      final Predicate<String> predicate) {
    return new F<Class<?>, Boolean>() {

      public Boolean apply(final Class<?> a) {
        return predicate.apply(a.getName());
      }

    };
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
        new ClassPathByteArraySource(getClassPath()),
        this.data.getDependencyAnalysisMaxDistance());
    final Map<String, List<ClassGrouping>> testToCodeMap = new HashMap<String, List<ClassGrouping>>();
    for (final String each : tests) {
      final Set<String> testReach = analyser
          .extractCallDependenciesForPackages(each,
              this.data.getClassesInScopeFilter());
      System.out.println("Found " + testReach.size() + " class hits for "
          + each);

      final List<ClassGrouping> group = flatMap(testReach,
          jvmClassToGroup(groupedByOuterClass));
      if (group != null) {
        testToCodeMap.put(each, group);
      } else {
        System.out.println("No group found for " + each);
      }

    }
    return testToCodeMap;
  }

  protected ClassPath getClassPath() {
    return this.data.getClassPath(this.nonLocalClassPath).getOrElse(
        new ClassPath());
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

  private Collection<String> convertClassesToStrings(
      final Collection<Class<?>> classes) {
    return FCollection.map(classes, Functions.classToName());
  }

  private List<TestUnit> createMutationTestUnits(
      final Map<ClassGrouping, List<String>> groupedClassesToTests,
      final Configuration pitConfig) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    for (final Entry<ClassGrouping, List<String>> each : groupedClassesToTests
        .entrySet()) {
      tus.add(createMutationTestUnit(each.getKey(), each.getValue(), pitConfig));

    }
    return tus;
  }

  private TestUnit createMutationTestUnit(final ClassGrouping classGrouping,
      final List<String> testClasses, final Configuration pitConfig) {

    final MutationEngine engine = DefaultMutationConfigFactory
        .createEngine(this.data.getMutators().toArray(
            new Mutator[this.data.getMutators().size()]));
    final MutationConfig mutationConfig = new MutationConfig(engine,
        MutationTestType.CODE_CENTRIC, 0, this.data.getJvmArgs());
    final Description d = new Description("mutation test of "
        + classGrouping.getParent(), MutationCoverageReport.class, null);
    final List<String> codeClasses = map(classGrouping, jvmClassToClassName());
    return new InstrumentedMutationTestUnit(testClasses, codeClasses,
        mutationConfig, pitConfig, d, this.javaAgentFinder);
  }

  private List<Class<?>> extractCodeClasses(final Collection<Class<?>> targets,
      final Collection<Class<?>> tests) {
    final List<Class<?>> cs = new ArrayList<Class<?>>();
    cs.addAll(targets);
    cs.removeAll(tests);
    return cs;
  }

  private Map<String, ClassGrouping> groupByOuterClass(
      final Collection<Class<?>> classes) {
    final Map<String, ClassGrouping> group = new HashMap<String, ClassGrouping>();
    forEach(classes, addToMapIfTopLevelClass(group));

    forEach(classes, addToParentGrouping(group));

    return group;

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

  private SideEffect1<Class<?>> addToMapIfTopLevelClass(
      final Map<String, ClassGrouping> map) {
    return new SideEffect1<Class<?>>() {

      public void apply(final Class<?> clazz) {
        if (Reflection.isTopClass(clazz)) {
          map.put(clazz.getName(), new ClassGrouping(clazz.getName(),
              Collections.<String> emptyList()));
        } else {
          System.out.println("Not top level " + clazz);
        }

      }

    };
  }

  private Collection<String> classesWithoutATest(
      final List<Class<?>> codeClasses,
      final Map<ClassGrouping, List<String>> codeToTests) {
    final FunctionalList<String> codeWithTests = FCollection.flatMap(
        codeToTests.keySet(), id(ClassGrouping.class));

    final FunctionalList<String> classesWithoutTest = FCollection.map(
        codeClasses, classToName());
    classesWithoutTest.removeAll(codeWithTests);
    return classesWithoutTest;

  }

}
