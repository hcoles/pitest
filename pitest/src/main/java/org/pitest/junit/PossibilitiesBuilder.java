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

package org.pitest.junit;

import java.util.Arrays;
import java.util.List;

import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.IgnoredBuilder;
import org.junit.internal.builders.JUnit3Builder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.internal.builders.NullBuilder;
import org.junit.internal.builders.SuiteMethodBuilder;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;
import org.pitest.junit.adapter.PITJUnitRunner;

public class PossibilitiesBuilder extends RunnerBuilder {
  private final boolean canUseSuiteMethod;

  public PossibilitiesBuilder(final boolean canUseSuiteMethod) {
    this.canUseSuiteMethod = canUseSuiteMethod;
  }

  @Override
  public Runner runnerForClass(final Class<?> testClass) throws Throwable {
    final List<RunnerBuilder> builders = Arrays.asList(ignoredBuilder(),
        annotatedBuilder(), suiteMethodBuilder(), junit3Builder(),
        junit4Builder());

    for (final RunnerBuilder each : builders) {
      if (!((each instanceof AnnotatedBuilder) && isPitTest(testClass))) {
        final Runner runner = each.safeRunnerForClass(testClass);
        if (runner != null) {
          return runner;

        }
      }

    }
    return null;
  }

  private JUnit4Builder junit4Builder() {
    return new JUnit4Builder();
  }

  private JUnit3Builder junit3Builder() {
    return new JUnit3Builder();
  }

  private AnnotatedBuilder annotatedBuilder() {
    return new AnnotatedBuilder(this);
  }

  private IgnoredBuilder ignoredBuilder() {
    return new IgnoredBuilder();
  }

  private RunnerBuilder suiteMethodBuilder() {
    if (this.canUseSuiteMethod) {
      return new SuiteMethodBuilder();
    }
    return new NullBuilder();
  }

  private static boolean isPitTest(final Class<?> clazz) {
    final RunWith runner = clazz.getAnnotation(RunWith.class);
    if (runner != null) {
      return runner.value().isAssignableFrom(PITJUnitRunner.class);
    } else {
      return false;
    }
  }
}