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
package org.pitest.mutationtest.engine.gregor.config;

import java.util.Collection;

import org.pitest.functional.F;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.TryWithResourcesFilter;
import org.pitest.mutationtest.engine.gregor.inlinedcode.InlinedCodeFilter;
import org.pitest.mutationtest.engine.gregor.inlinedcode.InlinedFinallyBlockDetector;
import org.pitest.mutationtest.engine.gregor.inlinedcode.NoInlinedCodeDetection;

public final class GregorEngineFactory implements MutationEngineFactory {

  public MutationEngine createEngine(final boolean mutateStaticInitializers,
      final Predicate<String> excludedMethods,
      final Collection<String> loggingClasses,
      final Collection<String> mutators, final boolean detectInlinedCode) {
    return createEngineWithMutators(mutateStaticInitializers, excludedMethods,
        loggingClasses, createMutatorListFromArrayOrUseDefaults(mutators),
        detectInlinedCode);
  }

  public MutationEngine createEngineWithMutators(
      final boolean mutateStaticInitializers,
      final Predicate<String> excludedMethods,
      final Collection<String> loggingClasses,
      final Collection<? extends MethodMutatorFactory> mutators,
      final boolean detectInlinedCode) {

    final Predicate<MethodInfo> filter = pickFilter(mutateStaticInitializers,
        Prelude.not(stringToMethodInfoPredicate(excludedMethods)));
    final DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(
        filter, loggingClasses, mutators,
        inlinedCodeDetector(detectInlinedCode), tryWithResourcesFilter());
    return new GregorMutationEngine(config);
  }

  private static InlinedCodeFilter inlinedCodeDetector(
      final boolean detectInlinedCode) {
    if (detectInlinedCode) {
      return new InlinedFinallyBlockDetector();
    } else {
      return new NoInlinedCodeDetection();
    }
  }

  private static TryWithResourcesFilter tryWithResourcesFilter() {
    return new TryWithResourcesFilter();
  }

  private static Collection<? extends MethodMutatorFactory> createMutatorListFromArrayOrUseDefaults(
      final Collection<String> mutators) {
    if ((mutators != null) && !mutators.isEmpty()) {
      return Mutator.fromStrings(mutators);
    } else {
      return Mutator.DEFAULTS.asCollection();
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

  public String name() {
    return "gregor";
  }

  public String description() {
    return "Default mutation engine";
  }

}
