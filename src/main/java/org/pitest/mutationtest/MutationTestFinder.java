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

import org.pitest.ConcreteConfiguration;
import org.pitest.Description;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.TestClass;
import org.pitest.mutationtest.classloader.MutationTestUnit;
import org.pitest.mutationtest.hotswap.HotSwapMutationTestUnit;

public class MutationTestFinder implements TestUnitFinder {

  private final MutationConfig mutationConfig;

  public MutationTestFinder(final MutationConfig config) {
    this.mutationConfig = config;
  }

  public boolean canHandle(final Class<?> clazz, final boolean alreadyHandled) {
    return true;
  }

  public Collection<TestUnit> findTestUnits(final TestClass clazz,
      final Configuration configuration) {
    final Option<Class<?>> testee = determineTestee(clazz.getClazz());
    if (testee.hasSome()) {
      final Configuration updatedConfig = createCopyOfConfig(configuration);
      updatedConfig.testUnitFinders().remove(this);
      updatedConfig.configurationUpdaters().remove(
          MutationSuiteConfigUpdater.instance());

      final Description d = new Description("mutation test", clazz.getClazz(),
          null);

      final MutationConfig updatedMutationConfig = determineConfigToUse(clazz);

      return Collections.<TestUnit> singleton(createTestUnit(clazz.getClazz(),
          testee.value(), updatedMutationConfig, updatedConfig, d));
    } else {
      return Collections.emptyList();
    }
  }

  private TestUnit createTestUnit(final Class<?> test,
      final Class<?> classToMutate, final MutationConfig mutationConfig,
      final Configuration pitConfig, final Description description) {
    if (mutationConfig.isUseHotswap()) {
      return new HotSwapMutationTestUnit(test, classToMutate, mutationConfig,
          pitConfig, description);
    } else {
      return new MutationTestUnit(test, classToMutate, mutationConfig,
          pitConfig, description);
    }
  }

  private MutationConfig determineConfigToUse(final TestClass clazz) {
    final MutationTest annotation = clazz.getClazz().getAnnotation(
        MutationTest.class);
    if (annotation != null) {
      return new MutationConfig(annotation.useHotSwap(),
          annotation.threshold(), annotation.mutators());
    } else {
      return this.mutationConfig;
    }
  }

  private Option<Class<?>> determineTestee(final Class<?> test) {
    final org.pitest.annotations.TestClass annotation = test
        .getAnnotation(org.pitest.annotations.TestClass.class);
    if (annotation == null) {
      return determineTesteeFromName(test);
    } else {
      return Option.<Class<?>> someOrNone(annotation.value());
    }
  }

  private Option<Class<?>> determineTesteeFromName(final Class<?> test) {
    final String name = test.getName();
    final int testLength = "Test".length();
    if (name.endsWith("Test")) {
      final Option<Class<?>> guessed = tryName(name.substring(0, name.length()
          - testLength));
      if (guessed.hasSome()) {
        return guessed;
      }
    }

    final String className = getClassNameWithoutPackage(test);
    if (className.startsWith("Test")) {
      final String nameGuess = className.substring(testLength, className
          .length());
      final Option<Class<?>> guessed = tryName(test.getPackage().getName()
          + "." + nameGuess);
      if (guessed.hasNone()) {
        if (test.getEnclosingClass() != null) {
          return tryName(test.getEnclosingClass().getName() + "$" + nameGuess);
        }
      } else {
        return guessed;
      }
    }

    return Option.none();
  }

  private String getClassNameWithoutPackage(final Class<?> clazz) {
    if (clazz.getEnclosingClass() == null) {
      return clazz.getName().substring(
          clazz.getPackage().getName().length() + 1, clazz.getName().length());
    } else {
      return clazz.getName().substring(
          clazz.getEnclosingClass().getName().length() + 1,
          clazz.getName().length());
    }
  }

  private Option<Class<?>> tryName(final String name) {
    try {
      final Class<?> guessed = Class.forName(name, true, IsolationUtils
          .getContextClassLoader());
      return Option.<Class<?>> someOrNone(guessed);
    } catch (final ClassNotFoundException e) {
      return Option.none();
    } catch (final NoClassDefFoundError e) {
      // not clear why we get this occasionally
      // when running with eclipse
      return Option.none();
    }
  }

  private Configuration createCopyOfConfig(final Configuration configuration) {
    return new ConcreteConfiguration(configuration);
  }

}
