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
import java.util.function.Predicate;

import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.EngineArguments;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.engine.MutationEngine;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.util.Glob;

public final class GregorEngineFactory implements MutationEngineFactory {

  @Override
  public MutationEngine createEngine(EngineArguments args) {
    return createEngineWithMutators(args.excludedMethods(),
           createMutatorListFromArrayOrUseDefaults(args.mutators()));
  }

  public MutationEngine createEngineWithMutators(
      final Collection<String> excludedMethods,
      final Collection<? extends MethodMutatorFactory> mutators) {

    final Predicate<MethodInfo> filter = Prelude.not(stringToMethodInfoPredicate(excludedMethods));
    final DefaultMutationEngineConfiguration config = new DefaultMutationEngineConfiguration(
        filter, mutators);
    return new GregorMutationEngine(config);
  }

  private static Collection<? extends MethodMutatorFactory> createMutatorListFromArrayOrUseDefaults(
      final Collection<String> mutators) {
    if ((mutators != null) && !mutators.isEmpty()) {
      return Mutator.fromStrings(mutators);
    } else {
      return Mutator.defaults();
    }

  }

  private static Predicate<MethodInfo> stringToMethodInfoPredicate(
      final Collection<String> excludedMethods) {
    final Predicate<String> excluded = Prelude.or(Glob.toGlobPredicates(excludedMethods));
    return a -> excluded.test(a.getName());
  }

  @Override
  public String name() {
    return "gregor";
  }

  @Override
  public String description() {
    return "Default mutation engine";
  }

}
