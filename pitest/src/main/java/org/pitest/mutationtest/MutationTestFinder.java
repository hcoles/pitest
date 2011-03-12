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

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import org.pitest.ConcreteConfiguration;
import org.pitest.Description;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.extension.common.NoTestFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.internal.TestClass;
import org.pitest.mutationtest.instrument.InstrumentedMutationTestUnit;
import org.pitest.mutationtest.instrument.JavaAgentJarFinder;
import org.pitest.util.JavaAgent;
import org.pitest.util.TestInfo;
import org.pitest.util.Unchecked;

public class MutationTestFinder implements TestUnitFinder {

  private final MutationConfig                  mutationConfig;
  private final F<Class<?>, Collection<String>> findChildClassesStrategy;
  private final JavaAgent                       javaAgentFinder;

  public MutationTestFinder(final MutationConfig config) {
    this(config, new FindInnerAndMemberClassesStrategy(),
        new JavaAgentJarFinder());
  }

  public MutationTestFinder(final MutationConfig config,
      final F<Class<?>, Collection<String>> findChildClassesStrategy,
      final JavaAgent javaAgentFinder) {
    this.mutationConfig = config;
    this.findChildClassesStrategy = findChildClassesStrategy;
    this.javaAgentFinder = javaAgentFinder;
  }

  public Collection<TestUnit> findTestUnits(final TestClass clazz,
      final Configuration configuration, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {
    final Collection<Class<?>> testees = TestInfo.determineTestee(clazz
        .getClazz());

    final Collection<String> testeeNames = FCollection.flatMap(testees,
        this.findChildClassesStrategy);

    if (!testees.isEmpty()) {
      final ConcreteConfiguration updatedConfig = createCopyOfConfig(configuration);
      updatedConfig.setMutationTestFinder(new NoTestFinder());
      updatedConfig.configurationUpdaters().remove(
          MutationSuiteConfigUpdater.instance());

      final Description d = new Description("mutation test", clazz.getClazz(),
          null);

      final MutationConfig updatedMutationConfig = determineConfigToUse(clazz);

      final Set<TestUnit> units = Collections
          .<TestUnit> singleton(createTestUnit(clazz.getClazz(), testeeNames,
              updatedMutationConfig, updatedConfig, d));
      listener.receiveTests(units);
      // skip processing for mutation tests . . . yes?
      return units;
    } else {
      return Collections.emptyList();
    }
  }

  private TestUnit createTestUnit(final Class<?> test,
      final Collection<String> classesToMutate,
      final MutationConfig mutationConfig, final Configuration pitConfig,
      final Description description) {
    return new InstrumentedMutationTestUnit(Collections.<String> singleton(test
        .getName()), classesToMutate, this.javaAgentFinder, mutationConfig,
        pitConfig, description);

  }

  private MutationConfig determineConfigToUse(final TestClass clazz) {
    final MutationTest annotation = clazz.getClazz().getAnnotation(
        MutationTest.class);
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
