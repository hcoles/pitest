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

import org.pitest.functional.F;
import org.pitest.functional.Prelude;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.DefaultMutationEngineConfiguration;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

public final class DefaultMutationConfigFactory {

  public final static Collection<MethodMutatorFactory> DEFAULT_MUTATORS = Arrays
                                                                            .<MethodMutatorFactory> asList(
                                                                                Mutator.NEGATE_CONDITIONALS,
                                                                                Mutator.CONDITIONALS_BOUNDARY,
                                                                                Mutator.INCREMENTS,
                                                                                Mutator.MATH,
                                                                                Mutator.RETURN_VALS,
                                                                                Mutator.VOID_METHOD_CALLS,
                                                                                Mutator.INVERT_NEGS);

  public final static Collection<String>               LOGGING_CLASSES  = Arrays
                                                                            .asList(
                                                                                "java.util.logging",
                                                                                "org.apache.log4j",
                                                                                "org.slf4j",
                                                                                "org.apache.commons.logging");

  public static MutationEngine createEngine(
      final boolean mutateStaticInitializers,
      final Predicate<String> excludedMethods,
      final Collection<String> loggingClasses,
      final MethodMutatorFactory... mutators) {
    final Collection<MethodMutatorFactory> ms = createMutatorListFromArrayOrUseDefaults(mutators);
    final Predicate<MethodInfo> filter = pickFilter(mutateStaticInitializers,
        Prelude.not(stringToMethodInfoPredicate(excludedMethods)));
    final DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(
        filter, loggingClasses, ms);
    return new GregorMutationEngine(config);
  }

  private static Collection<MethodMutatorFactory> createMutatorListFromArrayOrUseDefaults(
      final MethodMutatorFactory... mutators) {
    if (mutators.length != 0) {
      return Arrays.<MethodMutatorFactory> asList(mutators);
    } else {
      return DEFAULT_MUTATORS;
    }

  }

  @SuppressWarnings("unchecked")
  private static Predicate<MethodInfo> pickFilter(
      final boolean mutateStaticInitializers,
      final Predicate<MethodInfo> excludedMethods) {
    if (!mutateStaticInitializers) {
      return Prelude.and(excludedMethods, notStaticInitializer());
    } else {
      return excludedMethods;
    }
  }

  private static F<MethodInfo, Boolean> stringToMethodInfoPredicate(
      final Predicate<String> excludedMethods) {
    return new Predicate<MethodInfo>() {

      public Boolean apply(final MethodInfo a) {
        return excludedMethods.apply(a.getName());
      }

    };
  }

  private static Predicate<MethodInfo> notStaticInitializer() {
    return new Predicate<MethodInfo>() {

      public Boolean apply(final MethodInfo a) {
        return !a.isStaticInitializer();
      }

    };
  }

};
