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

import org.pitest.ConcreteConfiguration;
import org.pitest.DefaultStaticConfig;
import org.pitest.Description;
import org.pitest.Pitest;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.ConsoleResultListener;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassPath;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.instrument.CoverageSource;
import org.pitest.mutationtest.instrument.InstrumentedMutationTestUnit;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;
import org.pitest.reflection.Reflection;
import org.pitest.util.JavaAgent;

public class CodeCentricReport extends MutationCoverageReport {

  private final ListenerFactory listenerFactory;
  private final JavaAgent       javaAgentFinder;
  private final boolean         nonLocalClassPath;

  public CodeCentricReport(final ReportOptions data,
      final JavaAgent javaAgentFinder, final ListenerFactory listenerFactory,
      final boolean nonLocalClassPath) {
    super(data);
    this.javaAgentFinder = javaAgentFinder;
    this.nonLocalClassPath = nonLocalClassPath;
    this.listenerFactory = listenerFactory;
  }

  @Override
  public void runReport() throws IOException {

    final long t0 = System.currentTimeMillis();

    final Collection<Class<?>> completeClassPath = flatMap(getClassPath()
        .findClasses(this.data.getClassesInScopeFilter()), stringToClass());

    final FunctionalCollection<Class<?>> tests = filter(completeClassPath,
        isWithinATestClass()); // note are looking for code within a test class
                               // - not quite the same thing as isATest

    final List<Class<?>> codeClasses = filter(
        extractCodeClasses(completeClassPath, tests),
        convertStringToClassFilter(this.data.getTargetClassesFilter()));

    final Map<String, ClassGrouping> groupedByOuterClass = groupByOuterClass(codeClasses);

    final ConcreteConfiguration initialConfig = new ConcreteConfiguration(
        new JUnitCompatibleConfiguration());
    final CoverageDatabase coverageDatabase = new DefaultCoverageDatabase(
        initialConfig, this.getClassPath(), this.javaAgentFinder, this.data);

    coverageDatabase.initialise(tests);

    final Map<ClassGrouping, List<String>> codeToTests = coverageDatabase
        .mapCodeToTests(tests, groupedByOuterClass);

    final DefaultStaticConfig staticConfig = new DefaultStaticConfig();
    final TestListener mutationReportListener = this.listenerFactory
        .getListener(this.data);

    staticConfig.addTestListener(mutationReportListener);
    staticConfig.addTestListener(new ConsoleResultListener());

    reportFailureForClassesWithoutTests(
        classesWithoutATest(codeClasses, codeToTests), mutationReportListener);

    final List<TestUnit> tus = createMutationTestUnits(codeToTests,
        initialConfig, coverageDatabase);

    System.out.println("Created  " + tus.size() + " mutation test units");

    final Pitest pit = new Pitest(staticConfig, initialConfig);
    pit.run(new UnContainer(), tus);

    System.out.println("Completed in " + timeSpan(t0) + ".  Tested "
        + codeToTests.size() + " classes.");

  }

  private String timeSpan(final long t0) {
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

  protected ClassPath getClassPath() {
    return this.data.getClassPath(this.nonLocalClassPath).getOrElse(
        new ClassPath());
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

  private List<TestUnit> createMutationTestUnits(
      final Map<ClassGrouping, List<String>> groupedClassesToTests,
      final Configuration pitConfig, final CoverageDatabase coverageDatabase) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    for (final Entry<ClassGrouping, List<String>> codeToTests : groupedClassesToTests
        .entrySet()) {
      tus.add(createMutationTestUnit(
          codeToTests.getKey(),
          coverageDatabase.getCoverage(codeToTests.getKey(),
              codeToTests.getValue())));

    }
    return tus;
  }

  private TestUnit createMutationTestUnit(final ClassGrouping classGrouping,
      final CoverageSource coverageSource) {

    final MutationEngine engine = DefaultMutationConfigFactory.createEngine(
        this.data.isMutateStaticInitializers(), this.data.getMutators()
            .toArray(new Mutator[this.data.getMutators().size()]));
    final MutationConfig mutationConfig = new MutationConfig(engine,
        MutationTestType.CODE_CENTRIC, 0, this.data.getJvmArgs());
    final Description d = new Description("mutation test of "
        + classGrouping.getParent(), MutationCoverageReport.class, null);
    final List<String> codeClasses = map(classGrouping, jvmClassToClassName());
    return new InstrumentedMutationTestUnit(codeClasses, mutationConfig, d,
        this.javaAgentFinder, coverageSource);
  }

  private List<Class<?>> extractCodeClasses(final Collection<Class<?>> targets,
      final Collection<Class<?>> tests) {
    final List<Class<?>> cs = new ArrayList<Class<?>>();
    cs.addAll(targets);
    cs.removeAll(tests);
    return cs;
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
