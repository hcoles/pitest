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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.pitest.ConcreteConfiguration;
import org.pitest.Description;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.SuppressMutationTestFinding;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.internal.ClassPath;
import org.pitest.mutationtest.instrument.CoverageSource;
import org.pitest.mutationtest.instrument.InstrumentedMutationTestUnit;
import org.pitest.mutationtest.instrument.JavaAgentJarFinder;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.testunit.FailingTestUnit;
import org.pitest.util.Glob;
import org.pitest.util.JavaAgent;
import org.pitest.util.TestInfo;
import org.pitest.util.Unchecked;

public class MutationTestFinder implements TestUnitFinder {

  private final MutationConfig                  mutationConfig;
  private final F<Class<?>, Collection<String>> findChildClassesStrategy;
  private final JavaAgent                       javaAgentFinder;
  private final ClassPath                       classPath = new ClassPath();

  // private final ReportOptions data;

  public MutationTestFinder(final MutationConfig config) {
    this(config, new FindInnerAndMemberClassesStrategy(),
        new JavaAgentJarFinder());
  }

  // private static ReportOptions defaultOptions() {
  // ReportOptions data = new ReportOptions();
  // data.setClassesInScope(Collections.singleton(True.<String> all()));
  // data.setTargetClasses(Collections.singleton(True.<String> all()));
  // data.setTargetTests(Collections.singleton(True.<String> all()));
  // return data;
  // }

  public MutationTestFinder(final MutationConfig config,
      final F<Class<?>, Collection<String>> findChildClassesStrategy,
      final JavaAgent javaAgentFinder) {
    this.mutationConfig = config;
    this.findChildClassesStrategy = findChildClassesStrategy;
    this.javaAgentFinder = javaAgentFinder;
  }

  public Collection<TestUnit> findTestUnits(final Class<?> clazz,
      final Configuration configuration, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {
    final Collection<Class<?>> testees = TestInfo.determineTestee(clazz);

    final Collection<String> testeeNames = FCollection.flatMap(testees,
        this.findChildClassesStrategy);

    if (!testees.isEmpty()) {
      final Collection<TestUnit> units = createUnits(clazz, configuration,
          testeeNames);
      listener.receiveTests(units);
      return units;
    } else {
      return Collections.emptyList();
    }
  }

  private Collection<TestUnit> createUnits(final Class<?> clazz,
      final Configuration configuration, final Collection<String> testeeNames) {
    final ConcreteConfiguration updatedConfig = createCopyOfConfig(configuration);
    updatedConfig.setMutationTestFinder(new SuppressMutationTestFinding());

    final Description d = new Description("mutation test", clazz, null);

    final MutationConfig updatedMutationConfig = determineConfigToUse(clazz);

    final ReportOptions data = new ReportOptions();
    data.setTargetClasses(FCollection.map(testeeNames, Glob.toGlobPredicate()));
    data.setClassesInScope(FCollection.map(testeeNames, Glob.toGlobPredicate()));
    // data.setTargetTests();

    final DefaultCoverageDatabase dcb = new DefaultCoverageDatabase(
        updatedConfig, this.classPath, this.javaAgentFinder, data);
    final List<Class<?>> classes = new ArrayList<Class<?>>();
    classes.add(clazz);
    final boolean allTestsGreen = dcb.initialise(classes);
    if (allTestsGreen) {

      final ClassGrouping code = new ClassGrouping(testeeNames.iterator()
          .next(), testeeNames);
      final CoverageSource s = dcb.getCoverage(code,
          Collections.singletonList(clazz.getName()));

      final Set<TestUnit> units = Collections
          .<TestUnit> singleton(createTestUnit(testeeNames,
              updatedMutationConfig, updatedConfig, d, s));
      // skip processing for mutation tests . . . yes?
      return units;
    } else {
      return Collections.<TestUnit> singletonList(new FailingTestUnit(d,
          "Cannot create mutation test as test do not run green"));
    }
  }

  private TestUnit createTestUnit(final Collection<String> classesToMutate,
      final MutationConfig mutationConfig, final Configuration pitConfig,
      final Description description, final CoverageSource source) {
    return new InstrumentedMutationTestUnit(classesToMutate, mutationConfig,
        description, this.javaAgentFinder, source,
        new PercentAndConstantTimeoutStrategy(1.25f, 1000));

  }

  private MutationConfig determineConfigToUse(final Class<?> clazz) {
    final MutationTest annotation = clazz.getAnnotation(MutationTest.class);
    if (annotation != null) {
      MutationConfigFactory factory;
      try {
        factory = annotation.mutationConfigFactory().newInstance();
      } catch (final InstantiationException e) {
        throw Unchecked.translateCheckedException(e);
      } catch (final IllegalAccessException e) {
        throw Unchecked.translateCheckedException(e);
      }
      return factory.createConfig(annotation);
    } else {
      return this.mutationConfig;
    }
  }

  private ConcreteConfiguration createCopyOfConfig(
      final Configuration configuration) {
    return new ConcreteConfiguration(configuration);
  }

}
