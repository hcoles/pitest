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

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.pitest.functional.F;
import org.pitest.functional.FArray;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.True;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;

public enum Mutator implements Iterable<MethodMutatorFactory> {

  /**
   * Default mutator that inverts the negation of integer and floating point
   * numbers.
   */
  INVERT_NEGS(InvertNegsMutator.INVERT_NEGS_MUTATOR),

  /**
   * Default mutator that mutates the return values of methods.
   */
  RETURN_VALS(ReturnValsMutator.RETURN_VALS_MUTATOR),

  /**
   * Optional mutator that mutates integer and floating point inline constants.
   */
  INLINE_CONSTS(new InlineConstantMutator()),

  /**
   * Default mutator that mutates binary arithmetic operations.
   */
  MATH(MathMutator.MATH_MUTATOR),

  /**
   * Default mutator that removes method calls to void methods.
   * 
   */
  VOID_METHOD_CALLS(VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR),

  /**
   * Default mutator that negates conditionals.
   */
  NEGATE_CONDITIONALS(NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR),

  /**
   * Default mutator that replaces the relational operators with their boundary
   * counterpart.
   */
  CONDITIONALS_BOUNDARY(
      ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR),

  /**
   * Default mutator that mutates increments, decrements and assignment
   * increments and decrements of local variables.
   */
  INCREMENTS(IncrementsMutator.INCREMENTS_MUTATOR),

  /**
   * Optional mutator that removes local variable increments.
   */

  REMOVE_INCREMENTS(RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR),

  /**
   * Optional mutator that removes method calls to non void methods.
   */
  NON_VOID_METHOD_CALLS(NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR),

  /**
   * Optional mutator that replaces constructor calls with null values.
   */
  CONSTRUCTOR_CALLS(ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR),

  /**
   * Removes conditional statements so that guarded statements always execute
   */
  REMOVE_CONDITIONALS(RemoveConditionalMutator.REMOVE_CONDITIONALS_MUTATOR),

  /**
   * Experimental mutator that removed assignments to member variables.
   */
  EXPERIMENTAL_MEMBER_VARIABLE(
      new org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator()),

  EXPERIMENTAL_SWITCH(
      new org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator()),
  /**
   * Default mutators
   */
  DEFAULTS(INVERT_NEGS, RETURN_VALS, MATH, VOID_METHOD_CALLS,
      NEGATE_CONDITIONALS, CONDITIONALS_BOUNDARY, INCREMENTS),
      
  STONGER(DEFAULTS, REMOVE_CONDITIONALS, EXPERIMENTAL_SWITCH),

  /**
   * All the mutators
   */
  ALL(DEFAULTS, NON_VOID_METHOD_CALLS, CONSTRUCTOR_CALLS, INLINE_CONSTS,
      EXPERIMENTAL_MEMBER_VARIABLE, EXPERIMENTAL_SWITCH, REMOVE_CONDITIONALS);

  Mutator(final Mutator... groups) {
    this.impls = asCollection(groups);
  }

  Mutator(final MethodMutatorFactory... impls) {
    this.impls = Arrays.asList(impls);
  }

  private final Iterable<MethodMutatorFactory> impls;

  public Iterator<MethodMutatorFactory> iterator() {
    return this.impls.iterator();
  }

  public Collection<MethodMutatorFactory> asCollection() {
    return FCollection.filter(this.impls, True.<MethodMutatorFactory> all());
  }

  public static Collection<MethodMutatorFactory> asCollection(
      final Mutator... groupings) {
    return FArray.flatMap(groupings, Prelude.id(Mutator.class));
  }

  public static Collection<MethodMutatorFactory> asCollection(
      final Collection<? extends Mutator> groups) {
    return FCollection.flatMap(groups, Prelude.id(Mutator.class));
  }

  public static Collection<MethodMutatorFactory> fromStrings(
      final Collection<String> names) {
    return FCollection.flatMap(names, fromString());
  }

  private static F<String, Iterable<MethodMutatorFactory>> fromString() {
    return new F<String, Iterable<MethodMutatorFactory>>() {
      public Iterable<MethodMutatorFactory> apply(final String a) {
        return valueOf(a);
      }
    };
  }

}
