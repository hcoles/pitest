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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;

public final class DefaultMutationConfigFactory implements
    MutationConfigFactory {

  private final static Predicate<MethodInfo>           filter           = True
                                                                            .<MethodInfo> all();

  public final static Collection<MethodMutatorFactory> DEFAULT_MUTATORS = Arrays
                                                                            .<MethodMutatorFactory> asList(
                                                                                Mutator.NEGATE_CONDITIONALS,
                                                                                Mutator.INCREMENTS,
                                                                                Mutator.MATH,
                                                                                Mutator.RETURN_VALS,
                                                                                Mutator.VOID_METHOD_CALLS,
                                                                                Mutator.NEGS);

  private final static Collection<String>              LOGGING_CLASSES  = Arrays
                                                                            .asList(
                                                                                "java.util.logging",
                                                                                "org.apache.log4j",
                                                                                "org.slf4j",
                                                                                "org.apache.commons.logging");

  public static MutationEngine makeDefaultEngine() {
    return new GregorMutationEngine(DEFAULT_MUTATORS, LOGGING_CLASSES, filter);
  }

  public MutationConfig createConfig(final MutationTest annotation) {
    return new MutationConfig(createEngine(annotation),
        MutationTestType.TEST_CENTRIC, annotation.threshold(),
        Arrays.asList(annotation.jvmArgs()));
  }

  public static MutationConfig createConfig(final int threshold,
      final Mutator... mutators) {
    return new MutationConfig(createEngine(true, mutators),
        MutationTestType.TEST_CENTRIC, threshold,
        Collections.<String> emptyList());
  }

  private MutationEngine createEngine(final MutationTest annotation) {
    return createEngine(true, annotation.mutators());
  }

  public static MutationEngine createEngine(
      final boolean mutateStaticInitializers, final Mutator... mutators) {
    final Collection<MethodMutatorFactory> ms = createMutatorListFromArrayOrUseDefaults(mutators);
    final Predicate<MethodInfo> filter = pickFilter(mutateStaticInitializers);
    return new GregorMutationEngine(ms, LOGGING_CLASSES, filter);
  }

  private static Predicate<MethodInfo> pickFilter(
      final boolean mutateStaticInitializers) {
    if (!mutateStaticInitializers) {
      return new Predicate<MethodInfo>() {

        public Boolean apply(final MethodInfo a) {
          return !a.isStaticInitializer();
        }

      };
    } else {
      return filter;
    }
  }

  private static Collection<MethodMutatorFactory> createMutatorListFromArrayOrUseDefaults(
      final Mutator... mutators) {
    if (mutators.length != 0) {
      return Arrays.<MethodMutatorFactory> asList(mutators);
    } else {
      return DEFAULT_MUTATORS;
    }

  }

};
