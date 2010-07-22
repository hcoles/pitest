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
import org.pitest.internal.TestClass;

public class MutationSuiteFinder implements TestUnitFinder {

  private final int            threshold;
  private final MutationConfig mutationConfig;

  public MutationSuiteFinder(final int threshold, final MutationConfig config) {
    this.threshold = threshold;
    this.mutationConfig = config;
  }

  public boolean canHandle(final boolean alreadyHandled) {
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
      return Collections.<TestUnit> singleton(new MutationSuiteTestUnit(clazz
          .getClazz(), testee.value(), this.mutationConfig, updatedConfig, d,
          this.threshold));
    } else {
      return Collections.emptyList();
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
        return tryName(test.getEnclosingClass().getName() + "$" + nameGuess);
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
      final Class<?> guessed = Class.forName(name, true, Thread.currentThread()
          .getContextClassLoader());
      return Option.<Class<?>> someOrNone(guessed);
    } catch (final ClassNotFoundException e) {
      return Option.none();
    }
  }

  private Configuration createCopyOfConfig(final Configuration configuration) {
    return new ConcreteConfiguration(configuration);
  }

}
