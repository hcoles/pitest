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
import java.util.EnumMap;
import java.util.Map;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;
import org.pitest.mutationtest.report.MutationTestSummaryData.MutationTestType;

public final class DefaultMutationConfigFactory implements
    MutationConfigFactory {

  private final static Predicate<MethodInfo>              filter           = True
                                                                               .<MethodInfo> all();

  private final static Map<Mutator, MethodMutatorFactory> enumMapping      = new EnumMap<Mutator, MethodMutatorFactory>(
                                                                               Mutator.class);

  public final static Collection<Mutator>                 DEFAULT_MUTATORS = Arrays
                                                                               .asList(
                                                                                   Mutator.CONDITIONALS,
                                                                                   Mutator.INCREMENTS,
                                                                                   Mutator.MATH,
                                                                                   Mutator.RETURN_VALS,
                                                                                   Mutator.METHOD_CALLS,
                                                                                   Mutator.NEGS);

  private final static Collection<String>                 LOGGING_CLASSES  = Arrays
                                                                               .asList(
                                                                                   "java.util.logging",
                                                                                   "org.apache.log4j",
                                                                                   "org.slf4j",
                                                                                   "org.apache.commons.logging");

  static {
    enumMapping.put(Mutator.CONDITIONALS,
        ConditionalsMutator.CONDITIONALS_MUTATOR);
    enumMapping
        .put(Mutator.METHOD_CALLS, MethodCallMutator.METHOD_CALL_MUTATOR);
    enumMapping.put(Mutator.INCREMENTS, IncrementsMutator.INCREMENTS_MUTATOR);
    enumMapping.put(Mutator.NEGS, InvertNegsMutator.INVERT_NEGS_MUTATOR);
    enumMapping.put(Mutator.MATH, MathMutator.MATH_MUTATOR);
    enumMapping.put(Mutator.RETURN_VALS, ReturnValsMutator.RETURN_VALS_MUTATOR);
  }

  public static MutationEngine makeDefaultEngine() {
    return new GregorMutationEngine(enumMapping.values(), LOGGING_CLASSES,
        filter);
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
    final Collection<Mutator> ms = createMutatorListFromArrayOrUseDefaults(mutators);
    final Predicate<MethodInfo> filter = pickFilter(mutateStaticInitializers);
    return new GregorMutationEngine(
        FCollection.map(ms, mutatorTokenToMutator()), LOGGING_CLASSES, filter);
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

  private static Collection<Mutator> createMutatorListFromArrayOrUseDefaults(
      final Mutator... mutators) {
    if (mutators.length != 0) {
      return Arrays.asList(mutators);
    } else {
      return DEFAULT_MUTATORS;
    }

  }

  private static F<Mutator, MethodMutatorFactory> mutatorTokenToMutator() {
    return new F<Mutator, MethodMutatorFactory>() {

      public MethodMutatorFactory apply(final Mutator a) {
        return enumMapping.get(a);
      }

    };
  }

};
