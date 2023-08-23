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

import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanFalseReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.BooleanTrueReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.EmptyObjectReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.returns.PrimitiveReturnsMutator;
import org.pitest.util.IsolationUtils;
import org.pitest.util.ServiceLoader;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pitest.functional.Streams.asStream;

public final class Mutator {

  private static final Map<String, List<MethodMutatorFactory>> MUTATORS;

  static {
    Collection<MutatorGroup> mutatorFactories = ServiceLoader.load(MutatorGroup.class, IsolationUtils.getContextClassLoader());

    Collection<MethodMutatorFactory> ms = ServiceLoader.load(MethodMutatorFactory.class, IsolationUtils.getContextClassLoader());

    MUTATORS = ms.stream()
            .collect(Collectors.groupingBy(MethodMutatorFactory::getName));

    mutatorFactories.stream()
            .forEach(m -> m.register(MUTATORS));
  }

  public static Collection<MethodMutatorFactory> all() {
    return fromStrings(allMutatorIds());
  }

  public static Collection<String> allMutatorIds() {
    return MUTATORS.keySet();
  }

  private static Collection<MethodMutatorFactory> combine(
      Collection<MethodMutatorFactory> a, Collection<MethodMutatorFactory> b) {
    final List<MethodMutatorFactory> l = new ArrayList<>(a);
    l.addAll(b);
    return l;
  }

  /**
   * Proposed new defaults - replaced the RETURN_VALS mutator with the new more stable set
   */
  public static Collection<MethodMutatorFactory> newDefaults() {
    return combine(group(InvertNegsMutator.INVERT_NEGS,
        MathMutator.MATH,
        VoidMethodCallMutator.VOID_METHOD_CALLS,
        NegateConditionalsMutator.NEGATE_CONDITIONALS,
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY,
        IncrementsMutator.INCREMENTS), betterReturns());
  }


  private static Collection<MethodMutatorFactory> betterReturns() {
    return group(BooleanTrueReturnValsMutator.TRUE_RETURNS,
        BooleanFalseReturnValsMutator.FALSE_RETURNS,
        PrimitiveReturnsMutator.PRIMITIVE_RETURNS,
        EmptyObjectReturnValsMutator.EMPTY_RETURNS,
        NullReturnValsMutator.NULL_RETURNS);
  }

  private static Collection<MethodMutatorFactory> group(
      final MethodMutatorFactory... ms) {
    return Arrays.asList(ms);
  }

  public static Collection<MethodMutatorFactory> byName(final String name) {
    // prevent null collection returns?
    return asStream(MUTATORS.get(name))
            .collect(Collectors.toList());
  }

  public static Collection<MethodMutatorFactory> fromStrings(
      final Collection<String> names) {

    List<String> exclusions = names.stream()
            .filter(s -> s.startsWith("-"))
            .map(s -> s.substring(1))
            .collect(Collectors.toList());

    List<String> inclusions = names.stream()
            .filter(s -> !s.startsWith("-"))
            .collect(Collectors.toList());

    Set<MethodMutatorFactory> unique = inclusions.stream()
            .flatMap(fromString(MUTATORS))
            .collect(Collectors.toCollection(() -> new TreeSet<>(compareId())));

    Set<MethodMutatorFactory> excluded = exclusions.stream()
            .flatMap(fromString(MUTATORS))
            .collect(Collectors.toCollection(() -> new TreeSet<>(compareId())));

    unique.removeAll(excluded);

    return unique;
  }

  private static Comparator<? super MethodMutatorFactory> compareId() {
    return Comparator.comparing(MethodMutatorFactory::getGloballyUniqueId);
  }

  private static Function<String, Stream<MethodMutatorFactory>> fromString(Map<String, List<MethodMutatorFactory>> mutators) {
    return a -> {

      if (a.equals("ALL")) {
        return all().stream();
      }

      final List<MethodMutatorFactory> i = mutators.get(a);
      if (i == null) {
        throw new PitHelpError(Help.UNKNOWN_MUTATOR, a);
      }
      return i.stream();
    };
  }

}
