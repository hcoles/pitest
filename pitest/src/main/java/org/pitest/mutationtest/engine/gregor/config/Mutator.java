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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ArgumentPropagationMutator;
import org.pitest.mutationtest.engine.gregor.mutators.BooleanFalseReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.BooleanTrueReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator.Choice;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.custom.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.custom.ConditionalsBoundaryMutator1;
import org.pitest.mutationtest.engine.gregor.mutators.custom.ConditionalsBoundaryMutator2;
import org.pitest.mutationtest.engine.gregor.mutators.custom.ConditionalsBoundaryMutator3;
import org.pitest.mutationtest.engine.gregor.mutators.custom.ConditionalsBoundaryMutator4;
import org.pitest.mutationtest.engine.gregor.mutators.custom.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.custom.MathMutator1;
import org.pitest.mutationtest.engine.gregor.mutators.custom.MathMutator2;
import org.pitest.mutationtest.engine.gregor.mutators.custom.MathMutator3;
import org.pitest.mutationtest.engine.gregor.mutators.custom.MathMutator4;
import org.pitest.mutationtest.engine.gregor.mutators.custom.NegationMutator;
import org.pitest.mutationtest.engine.gregor.mutators.custom.OperandReplacement;
import org.pitest.mutationtest.engine.gregor.mutators.custom.OperandReplacement1;
import org.pitest.mutationtest.engine.gregor.mutators.custom.PostDec;
import org.pitest.mutationtest.engine.gregor.mutators.custom.PostInc;
import org.pitest.mutationtest.engine.gregor.mutators.custom.M1;
import org.pitest.mutationtest.engine.gregor.mutators.custom.PreDec;
import org.pitest.mutationtest.engine.gregor.mutators.custom.PreInc;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator;


public final class Mutator {

  private static final Map<String, Iterable<MethodMutatorFactory>> MUTATORS = new LinkedHashMap<>();

  static {

      /**
       * Custom Math Mutators
       */
      add("MATH", MathMutator.MATH_MUTATOR);
      add("MATH1", MathMutator1.MATH_MUTATOR1);
      add("MATH2", MathMutator2.MATH_MUTATOR2);
      add("MATH3", MathMutator3.MATH_MUTATOR3);
      add("MATH4", MathMutator4.MATH_MUTATOR4);

      /**
       *  Custom Relational Mutator
       */
      add("CONDITIONALS_BOUNDARY", ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR);
      add("CONDITIONALS_BOUNDARY1", ConditionalsBoundaryMutator1.CONDITIONALS_BOUNDARY_MUTATOR1);
      add("CONDITIONALS_BOUNDARY2", ConditionalsBoundaryMutator2.CONDITIONALS_BOUNDARY_MUTATOR2);
      add("CONDITIONALS_BOUNDARY3", ConditionalsBoundaryMutator3.CONDITIONALS_BOUNDARY_MUTATOR3);
      add("CONDITIONALS_BOUNDARY4", ConditionalsBoundaryMutator4.CONDITIONALS_BOUNDARY_MUTATOR4);

      // AOD Mutators
      add("FIRST_OPERAND_REPLACEMENT_MUTATOR",OperandReplacement.OPERAND_REPLACEMENT_MUTATOR);
      add("SECOND_OPERAND_REPLACEMENT_MUTATOR",OperandReplacement1.OPERAND_REPLACEMENT_MUTATOR1);

      // Negation Mutator
      add("NEGATION_MUTATOR",NegationMutator.NEGATION_MUTATOR);

      // Increment and Decrement Mutators
      add("PRE_INC",PreInc.PRE_INC);
      add("PRE_DEC",PreDec.PRE_DEC);
      add("POST_INC",PostInc.POST_INC);
      add("POST_DEC",PostDec.POST_DEC);

      // M1_Mutator
      add("M1_MUTATOR", M1.M1_MUTATOR);


      /**
       * Default mutator that inverts the negation of integer and floating point
       * numbers.
       */
      add("INVERT_NEGS", InvertNegsMutator.INVERT_NEGS_MUTATOR);

      /**
       * Default mutator that mutates the return values of methods.
       */
      add("RETURN_VALS", ReturnValsMutator.RETURN_VALS_MUTATOR);

      /**
       * Optional mutator that mutates integer and floating point inline
       * constants.
       */
      add("INLINE_CONSTS", new InlineConstantMutator());


      /**
       * Default mutator that removes method calls to void methods.
       *
       */
      add("VOID_METHOD_CALLS", VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR);

      /**
       * Default mutator that negates conditionals.
       */
      add("NEGATE_CONDITIONALS",
              NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR);



      /**
       * Default mutator that mutates increments, decrements and assignment
       * increments and decrements of local variables.
       */
      add("INCREMENTS", IncrementsMutator.INCREMENTS_MUTATOR);

      /**
       * Optional mutator that removes local variable increments.
       */

      add("REMOVE_INCREMENTS", RemoveIncrementsMutator.REMOVE_INCREMENTS_MUTATOR);

      /**
       * Optional mutator that removes method calls to non void methods.
       */
      add("NON_VOID_METHOD_CALLS",
              NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR);

      /**
       * Optional mutator that replaces constructor calls with null values.
       */
      add("CONSTRUCTOR_CALLS", ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR);

      /**
       * Removes conditional statements so that guarded statements always execute
       * The EQUAL version ignores LT,LE,GT,GE, which is the default behaviour,
       * ORDER version mutates only those.
       */

      add("REMOVE_CONDITIONALS_EQ_IF", new RemoveConditionalMutator(Choice.EQUAL,
              true));
      add("REMOVE_CONDITIONALS_EQ_ELSE", new RemoveConditionalMutator(
              Choice.EQUAL, false));
      add("REMOVE_CONDITIONALS_ORD_IF", new RemoveConditionalMutator(
              Choice.ORDER, true));
      add("REMOVE_CONDITIONALS_ORD_ELSE", new RemoveConditionalMutator(
              Choice.ORDER, false));
      addGroup("REMOVE_CONDITIONALS", RemoveConditionalMutator.makeMutators());

      add("TRUE_RETURNS", BooleanTrueReturnValsMutator.BOOLEAN_TRUE_RETURN);
      add("FALSE_RETURNS", BooleanFalseReturnValsMutator.BOOLEAN_FALSE_RETURN);
      add("PRIMITIVE_RETURNS", PrimitiveReturnsMutator.PRIMITIVE_RETURN_VALS_MUTATOR);
      add("EMPTY_RETURNS", EmptyObjectReturnValsMutator.EMPTY_RETURN_VALUES);
      add("NULL_RETURNS", NullReturnValsMutator.NULL_RETURN_VALUES);
      addGroup("RETURNS", betterReturns());

      /**
       * Experimental mutator that removed assignments to member variables.
       */
      add("EXPERIMENTAL_MEMBER_VARIABLE",
              new org.pitest.mutationtest.engine.gregor.mutators.experimental.MemberVariableMutator());

      /**
       * Experimental mutator that swaps labels in switch statements
       */
      add("EXPERIMENTAL_SWITCH",
              new org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator());

      /**
       * Experimental mutator that replaces method call with one of its parameters
       * of matching type
       */
      add("EXPERIMENTAL_ARGUMENT_PROPAGATION",
              ArgumentPropagationMutator.ARGUMENT_PROPAGATION_MUTATOR);

      /**
       * Experimental mutator that replaces method call with this
       */
      add("EXPERIMENTAL_NAKED_RECEIVER", NakedReceiverMutator.NAKED_RECEIVER);

      addGroup("REMOVE_SWITCH", RemoveSwitchMutator.makeMutators());
      addGroup("DEFAULTS", defaults());
      addGroup("STRONGER", stronger());
      addGroup("ALL", all());
      addGroup("CUSTOM", custom());
  }

  public static Collection<MethodMutatorFactory> all() {
    return fromStrings(MUTATORS.keySet());
  }

  private static Collection<MethodMutatorFactory> stronger() {
    return combine(
        defaults(),
        group(new RemoveConditionalMutator(Choice.EQUAL, false),
            new SwitchMutator()));
  }

  private static Collection<MethodMutatorFactory> combine(
      Collection<MethodMutatorFactory> a, Collection<MethodMutatorFactory> b) {
    final List<MethodMutatorFactory> l = new ArrayList<>(a);
    l.addAll(b);
    return l;
  }

  /**
   * Default set of mutators - designed to provide balance between strength and
   * performance
   */
  public static Collection<MethodMutatorFactory> defaults()
  {
    return group(InvertNegsMutator.INVERT_NEGS_MUTATOR,
        ReturnValsMutator.RETURN_VALS_MUTATOR,
        VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR,
        NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR,
        IncrementsMutator.INCREMENTS_MUTATOR);
  }

    public static Collection<MethodMutatorFactory> custom() {
      return group( MathMutator.MATH_MUTATOR,
              MathMutator1.MATH_MUTATOR1, MathMutator2.MATH_MUTATOR2,
              MathMutator3.MATH_MUTATOR3, MathMutator4.MATH_MUTATOR4,
              ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR,
              ConditionalsBoundaryMutator1.CONDITIONALS_BOUNDARY_MUTATOR1,
              ConditionalsBoundaryMutator2.CONDITIONALS_BOUNDARY_MUTATOR2,
              ConditionalsBoundaryMutator3.CONDITIONALS_BOUNDARY_MUTATOR3,
              ConditionalsBoundaryMutator4.CONDITIONALS_BOUNDARY_MUTATOR4,
              OperandReplacement.OPERAND_REPLACEMENT_MUTATOR,
              OperandReplacement1.OPERAND_REPLACEMENT_MUTATOR1,
              NegationMutator.NEGATION_MUTATOR,
              PostDec.POST_DEC,PreDec.PRE_DEC,
              PreDec.PRE_DEC,PreInc.PRE_INC,
              M1.M1_MUTATOR
              );
    }

  private static Collection<MethodMutatorFactory> group(
          Iterable<MethodMutatorFactory> mutators) {
    List<MethodMutatorFactory> list = new ArrayList<>();
    mutators.forEach(list::add);
    return list;
  }

  public static Collection<MethodMutatorFactory> betterReturns() {
    return group(BooleanTrueReturnValsMutator.BOOLEAN_TRUE_RETURN,
        BooleanFalseReturnValsMutator.BOOLEAN_FALSE_RETURN,
        PrimitiveReturnsMutator.PRIMITIVE_RETURN_VALS_MUTATOR,
        EmptyObjectReturnValsMutator.EMPTY_RETURN_VALUES,
        NullReturnValsMutator.NULL_RETURN_VALUES);
  }

  private static Collection<MethodMutatorFactory> group(
      final MethodMutatorFactory... ms) {
    return Arrays.asList(ms);
  }

  public static Collection<MethodMutatorFactory> byName(final String name) {
    return FCollection.map(MUTATORS.get(name),
        Prelude.id(MethodMutatorFactory.class));
  }

  private static void add(final String key, final MethodMutatorFactory value) {
    MUTATORS.put(key, Collections.singleton(value));
  }

  private static void addGroup(final String key,
      final Iterable<MethodMutatorFactory> value) {
    MUTATORS.put(key, value);
  }

  public static Collection<MethodMutatorFactory> fromStrings(
      final Collection<String> names) {
    final Set<MethodMutatorFactory> unique = new TreeSet<>(
        compareId());

    FCollection.flatMapTo(names, fromString(), unique);
    return unique;
  }

  private static Comparator<? super MethodMutatorFactory> compareId() {
    return (o1, o2) -> o1.getGloballyUniqueId().compareTo(o2.getGloballyUniqueId());
  }

  private static Function<String, Iterable<MethodMutatorFactory>> fromString() {
    return a -> {
      final Iterable<MethodMutatorFactory> i = MUTATORS.get(a);
      if (i == null) {
        throw new PitHelpError(Help.UNKNOWN_MUTATOR, a);
      }
      return i;
    };
  }

}
