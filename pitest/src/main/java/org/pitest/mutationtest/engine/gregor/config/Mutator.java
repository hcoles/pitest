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
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.EmptyObjectReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NullReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.PrimitiveReturnsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator;
import org.pitest.mutationtest.engine.gregor.mutators.RemoveConditionalMutator.Choice;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.BigIntegerMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.NakedReceiverMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveIncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator;
import org.pitest.mutationtest.engine.gregor.mutators.experimental.SwitchMutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ABSMutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOD1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOD2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.AOR4Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR4Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR5Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.CRCR6Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.OBBN3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR4Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.ROR5Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI1Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI2Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI3Mutator;
import org.pitest.mutationtest.engine.gregor.mutators.rv.UOI4Mutator;

public final class Mutator {

  private static final Map<String, Iterable<MethodMutatorFactory>> MUTATORS = new LinkedHashMap<>();

  static {

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
     * Default mutator that mutates binary arithmetic operations.
     */
    add("MATH", MathMutator.MATH_MUTATOR);

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
     * Default mutator that replaces the relational operators with their
     * boundary counterpart.
     */
    add("CONDITIONALS_BOUNDARY",
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR);

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

    experimentalMutators();
    
    researchMutators();

    addGroup("REMOVE_SWITCH", RemoveSwitchMutator.makeMutators());
    addGroup("DEFAULTS", defaults());
    addGroup("STRONGER", stronger());
    addGroup("ALL", all());
    addGroup("NEW_DEFAULTS", newDefaults());
    addGroup("AOR", aor());
    addGroup("AOD", aod());
    addGroup("CRCR", crcr());
    addGroup("OBBN", obbn());
    addGroup("ROR", ror());
    addGroup("UOI", uoi());

  }
  
  /**
   * Mutators that have not yet been battle tested
   */
  private static void experimentalMutators() {
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
    
    /**
     * Experimental mutator that swaps big integer methods
     */
    add("EXPERIMENTAL_BIG_INTEGER", BigIntegerMutator.INSTANCE);
  }
  
  /**
   * "Research" mutators that makeup "PITrv" as described in
   * https://researchrepository.ucd.ie/bitstream/10197/7748/4/ISSTA_2016_Demo_Camera_ready.pdf
   */
  private static void researchMutators() {
    /**
     * mutators that mutate binary arithmetic operations.
     */
    add("AOR_1", AOR1Mutator.AOR_1_MUTATOR);
    add("AOR_2", AOR2Mutator.AOR_2_MUTATOR);
    add("AOR_3", AOR3Mutator.AOR_3_MUTATOR);
    add("AOR_4", AOR4Mutator.AOR_4_MUTATOR);

    /**
     * mutator that replaces a variable with its negation.
     */
    add("ABS", ABSMutator.ABS_MUTATOR);

    /**
     * mutators that replace a binary arithmetic operations with one of its members.
     */
    add("AOD1", AOD1Mutator.AOD_1_MUTATOR);
    add("AOD1", AOD2Mutator.AOD_2_MUTATOR);


    /**
     * mutators that replace an inline constant a with 0, 1, -1, a+1 or a-1 .
     */
    add("CRCR1", CRCR1Mutator.CRCR_1_MUTATOR);
    add("CRCR2", CRCR2Mutator.CRCR_2_MUTATOR);
    add("CRCR3", CRCR3Mutator.CRCR_3_MUTATOR);
    add("CRCR4", CRCR4Mutator.CRCR_4_MUTATOR);
    add("CRCR5", CRCR5Mutator.CRCR_5_MUTATOR);
    add("CRCR6", CRCR6Mutator.CRCR_6_MUTATOR);

    /**
     * mutators that replace an bitwise ands and ors.
     */
    add("OBBN1", OBBN1Mutator.OBBN_1_MUTATOR);
    add("OBBN2", OBBN2Mutator.OBBN_2_MUTATOR);
    add("OBBN3", OBBN3Mutator.OBBN_3_MUTATOR);

    /**
     * mutators that replace conditional operators.
     */
    add("ROR1", ROR1Mutator.ROR_1_MUTATOR);
    add("ROR2", ROR2Mutator.ROR_2_MUTATOR);
    add("ROR3", ROR3Mutator.ROR_3_MUTATOR);
    add("ROR4", ROR4Mutator.ROR_4_MUTATOR);
    add("ROR5", ROR5Mutator.ROR_5_MUTATOR);

    /**
     * mutators that insert increments.
     */
    add("UOI1", UOI1Mutator.UOI_1_MUTATOR);
    add("UOI2", UOI2Mutator.UOI_2_MUTATOR);
    add("UOI3", UOI3Mutator.UOI_3_MUTATOR);
    add("UOI4", UOI4Mutator.UOI_4_MUTATOR);
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
  public static Collection<MethodMutatorFactory> defaults() {
    return group(InvertNegsMutator.INVERT_NEGS_MUTATOR,
        ReturnValsMutator.RETURN_VALS_MUTATOR, MathMutator.MATH_MUTATOR,
        VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR,
        NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR,
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR,
        IncrementsMutator.INCREMENTS_MUTATOR);
  }

  /**
   * Proposed new defaults - replaced the RETURN_VALS mutator with the new more stable set
   */
  public static Collection<MethodMutatorFactory> newDefaults() {
    return combine(group(InvertNegsMutator.INVERT_NEGS_MUTATOR,
        MathMutator.MATH_MUTATOR,
        VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR,
        NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR,
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR,
        IncrementsMutator.INCREMENTS_MUTATOR), betterReturns());
  }


  public static Collection<MethodMutatorFactory> betterReturns() {
    return group(BooleanTrueReturnValsMutator.BOOLEAN_TRUE_RETURN,
        BooleanFalseReturnValsMutator.BOOLEAN_FALSE_RETURN,
        PrimitiveReturnsMutator.PRIMITIVE_RETURN_VALS_MUTATOR,
        EmptyObjectReturnValsMutator.EMPTY_RETURN_VALUES,
        NullReturnValsMutator.NULL_RETURN_VALUES);
  }

  public static Collection<MethodMutatorFactory> aor() {
    return group(AOR1Mutator.AOR_1_MUTATOR,
            AOR2Mutator.AOR_2_MUTATOR,
            AOR3Mutator.AOR_3_MUTATOR,
            AOR4Mutator.AOR_4_MUTATOR);
  }

  public static Collection<MethodMutatorFactory> aod() {
    return group(AOD1Mutator.AOD_1_MUTATOR,
            AOD2Mutator.AOD_2_MUTATOR);
  }

  public static Collection<MethodMutatorFactory> crcr() {
    return group(CRCR1Mutator.CRCR_1_MUTATOR,
            CRCR2Mutator.CRCR_2_MUTATOR,
            CRCR3Mutator.CRCR_3_MUTATOR,
            CRCR4Mutator.CRCR_4_MUTATOR,
            CRCR5Mutator.CRCR_5_MUTATOR,
            CRCR6Mutator.CRCR_6_MUTATOR);
  }

  public static Collection<MethodMutatorFactory> obbn() {
    return group(OBBN1Mutator.OBBN_1_MUTATOR,
            OBBN2Mutator.OBBN_2_MUTATOR,
            OBBN3Mutator.OBBN_3_MUTATOR);
  }

  public static Collection<MethodMutatorFactory> ror() {
    return group(ROR1Mutator.ROR_1_MUTATOR,
            ROR2Mutator.ROR_2_MUTATOR,
            ROR3Mutator.ROR_3_MUTATOR,
            ROR4Mutator.ROR_4_MUTATOR,
            ROR5Mutator.ROR_5_MUTATOR);
  }

  public static Collection<MethodMutatorFactory> uoi() {
    return group(UOI1Mutator.UOI_1_MUTATOR,
            UOI2Mutator.UOI_2_MUTATOR,
            UOI3Mutator.UOI_3_MUTATOR,
            UOI4Mutator.UOI_4_MUTATOR);
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
