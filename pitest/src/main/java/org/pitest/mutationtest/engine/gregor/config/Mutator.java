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
import org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator;

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

  EXPERIMENTAL_REMOVE_SWITCH0(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(0)),
  EXPERIMENTAL_REMOVE_SWITCH1(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(1)),
  EXPERIMENTAL_REMOVE_SWITCH2(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(2)),
  EXPERIMENTAL_REMOVE_SWITCH3(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(3)),
  EXPERIMENTAL_REMOVE_SWITCH4(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(4)),
  EXPERIMENTAL_REMOVE_SWITCH5(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(5)),
  EXPERIMENTAL_REMOVE_SWITCH6(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(6)),
  EXPERIMENTAL_REMOVE_SWITCH7(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(7)),
  EXPERIMENTAL_REMOVE_SWITCH8(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(8)),
  EXPERIMENTAL_REMOVE_SWITCH9(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(9)),
  EXPERIMENTAL_REMOVE_SWITCH10(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(10)),
  EXPERIMENTAL_REMOVE_SWITCH11(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(11)),
  EXPERIMENTAL_REMOVE_SWITCH12(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(12)),
  EXPERIMENTAL_REMOVE_SWITCH13(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(13)),
  EXPERIMENTAL_REMOVE_SWITCH14(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(14)),
  EXPERIMENTAL_REMOVE_SWITCH15(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(15)),
  EXPERIMENTAL_REMOVE_SWITCH16(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(16)),
  EXPERIMENTAL_REMOVE_SWITCH17(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(17)),
  EXPERIMENTAL_REMOVE_SWITCH18(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(18)),
  EXPERIMENTAL_REMOVE_SWITCH19(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(19)),
  EXPERIMENTAL_REMOVE_SWITCH20(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(20)),
  EXPERIMENTAL_REMOVE_SWITCH21(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(21)),
  EXPERIMENTAL_REMOVE_SWITCH22(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(22)),
  EXPERIMENTAL_REMOVE_SWITCH23(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(23)),
  EXPERIMENTAL_REMOVE_SWITCH24(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(24)),
  EXPERIMENTAL_REMOVE_SWITCH25(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(25)),
  EXPERIMENTAL_REMOVE_SWITCH26(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(26)),
  EXPERIMENTAL_REMOVE_SWITCH27(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(27)),
  EXPERIMENTAL_REMOVE_SWITCH28(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(28)),
  EXPERIMENTAL_REMOVE_SWITCH29(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(29)),
  EXPERIMENTAL_REMOVE_SWITCH30(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(30)),
  EXPERIMENTAL_REMOVE_SWITCH31(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(31)),
  EXPERIMENTAL_REMOVE_SWITCH32(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(32)),
  EXPERIMENTAL_REMOVE_SWITCH33(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(33)),
  EXPERIMENTAL_REMOVE_SWITCH34(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(34)),
  EXPERIMENTAL_REMOVE_SWITCH35(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(35)),
  EXPERIMENTAL_REMOVE_SWITCH36(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(36)),
  EXPERIMENTAL_REMOVE_SWITCH37(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(37)),
  EXPERIMENTAL_REMOVE_SWITCH38(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(38)),
  EXPERIMENTAL_REMOVE_SWITCH39(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(39)),
  EXPERIMENTAL_REMOVE_SWITCH40(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(40)),
  EXPERIMENTAL_REMOVE_SWITCH41(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(41)),
  EXPERIMENTAL_REMOVE_SWITCH42(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(42)),
  EXPERIMENTAL_REMOVE_SWITCH43(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(43)),
  EXPERIMENTAL_REMOVE_SWITCH44(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(44)),
  EXPERIMENTAL_REMOVE_SWITCH45(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(45)),
  EXPERIMENTAL_REMOVE_SWITCH46(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(46)),
  EXPERIMENTAL_REMOVE_SWITCH47(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(47)),
  EXPERIMENTAL_REMOVE_SWITCH48(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(48)),
  EXPERIMENTAL_REMOVE_SWITCH49(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(49)),
  EXPERIMENTAL_REMOVE_SWITCH50(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(50)),
  EXPERIMENTAL_REMOVE_SWITCH51(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(51)),
  EXPERIMENTAL_REMOVE_SWITCH52(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(52)),
  EXPERIMENTAL_REMOVE_SWITCH53(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(53)),
  EXPERIMENTAL_REMOVE_SWITCH54(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(54)),
  EXPERIMENTAL_REMOVE_SWITCH55(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(55)),
  EXPERIMENTAL_REMOVE_SWITCH56(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(56)),
  EXPERIMENTAL_REMOVE_SWITCH57(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(57)),
  EXPERIMENTAL_REMOVE_SWITCH58(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(58)),
  EXPERIMENTAL_REMOVE_SWITCH59(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(59)),
  EXPERIMENTAL_REMOVE_SWITCH60(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(60)),
  EXPERIMENTAL_REMOVE_SWITCH61(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(61)),
  EXPERIMENTAL_REMOVE_SWITCH62(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(62)),
  EXPERIMENTAL_REMOVE_SWITCH63(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(63)),
  EXPERIMENTAL_REMOVE_SWITCH64(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(64)),
  EXPERIMENTAL_REMOVE_SWITCH65(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(65)),
  EXPERIMENTAL_REMOVE_SWITCH66(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(66)),
  EXPERIMENTAL_REMOVE_SWITCH67(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(67)),
  EXPERIMENTAL_REMOVE_SWITCH68(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(68)),
  EXPERIMENTAL_REMOVE_SWITCH69(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(69)),
  EXPERIMENTAL_REMOVE_SWITCH70(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(70)),
  EXPERIMENTAL_REMOVE_SWITCH71(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(71)),
  EXPERIMENTAL_REMOVE_SWITCH72(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(72)),
  EXPERIMENTAL_REMOVE_SWITCH73(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(73)),
  EXPERIMENTAL_REMOVE_SWITCH74(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(74)),
  EXPERIMENTAL_REMOVE_SWITCH75(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(75)),
  EXPERIMENTAL_REMOVE_SWITCH76(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(76)),
  EXPERIMENTAL_REMOVE_SWITCH77(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(77)),
  EXPERIMENTAL_REMOVE_SWITCH78(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(78)),
  EXPERIMENTAL_REMOVE_SWITCH79(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(79)),
  EXPERIMENTAL_REMOVE_SWITCH80(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(80)),
  EXPERIMENTAL_REMOVE_SWITCH81(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(81)),
  EXPERIMENTAL_REMOVE_SWITCH82(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(82)),
  EXPERIMENTAL_REMOVE_SWITCH83(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(83)),
  EXPERIMENTAL_REMOVE_SWITCH84(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(84)),
  EXPERIMENTAL_REMOVE_SWITCH85(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(85)),
  EXPERIMENTAL_REMOVE_SWITCH86(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(86)),
  EXPERIMENTAL_REMOVE_SWITCH87(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(87)),
  EXPERIMENTAL_REMOVE_SWITCH88(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(88)),
  EXPERIMENTAL_REMOVE_SWITCH89(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(89)),
  EXPERIMENTAL_REMOVE_SWITCH90(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(90)),
  EXPERIMENTAL_REMOVE_SWITCH91(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(91)),
  EXPERIMENTAL_REMOVE_SWITCH92(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(92)),
  EXPERIMENTAL_REMOVE_SWITCH93(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(93)),
  EXPERIMENTAL_REMOVE_SWITCH94(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(94)),
  EXPERIMENTAL_REMOVE_SWITCH95(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(95)),
  EXPERIMENTAL_REMOVE_SWITCH96(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(96)),
  EXPERIMENTAL_REMOVE_SWITCH97(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(97)),
  EXPERIMENTAL_REMOVE_SWITCH98(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(98)),
  EXPERIMENTAL_REMOVE_SWITCH99(new org.pitest.mutationtest.engine.gregor.mutators.experimental.RemoveSwitchMutator(99)),

  REMOVE_SWITCH(
      EXPERIMENTAL_REMOVE_SWITCH0,
      EXPERIMENTAL_REMOVE_SWITCH1,
      EXPERIMENTAL_REMOVE_SWITCH2,
      EXPERIMENTAL_REMOVE_SWITCH3,
      EXPERIMENTAL_REMOVE_SWITCH4,
      EXPERIMENTAL_REMOVE_SWITCH5,
      EXPERIMENTAL_REMOVE_SWITCH6,
      EXPERIMENTAL_REMOVE_SWITCH7,
      EXPERIMENTAL_REMOVE_SWITCH8,
      EXPERIMENTAL_REMOVE_SWITCH9,
      EXPERIMENTAL_REMOVE_SWITCH10,
      EXPERIMENTAL_REMOVE_SWITCH11,
      EXPERIMENTAL_REMOVE_SWITCH12,
      EXPERIMENTAL_REMOVE_SWITCH13,
      EXPERIMENTAL_REMOVE_SWITCH14,
      EXPERIMENTAL_REMOVE_SWITCH15,
      EXPERIMENTAL_REMOVE_SWITCH16,
      EXPERIMENTAL_REMOVE_SWITCH17,
      EXPERIMENTAL_REMOVE_SWITCH18,
      EXPERIMENTAL_REMOVE_SWITCH19,
      EXPERIMENTAL_REMOVE_SWITCH20,
      EXPERIMENTAL_REMOVE_SWITCH21,
      EXPERIMENTAL_REMOVE_SWITCH22,
      EXPERIMENTAL_REMOVE_SWITCH23,
      EXPERIMENTAL_REMOVE_SWITCH24,
      EXPERIMENTAL_REMOVE_SWITCH25,
      EXPERIMENTAL_REMOVE_SWITCH26,
      EXPERIMENTAL_REMOVE_SWITCH27,
      EXPERIMENTAL_REMOVE_SWITCH28,
      EXPERIMENTAL_REMOVE_SWITCH29,
      EXPERIMENTAL_REMOVE_SWITCH30,
      EXPERIMENTAL_REMOVE_SWITCH31,
      EXPERIMENTAL_REMOVE_SWITCH32,
      EXPERIMENTAL_REMOVE_SWITCH33,
      EXPERIMENTAL_REMOVE_SWITCH34,
      EXPERIMENTAL_REMOVE_SWITCH35,
      EXPERIMENTAL_REMOVE_SWITCH36,
      EXPERIMENTAL_REMOVE_SWITCH37,
      EXPERIMENTAL_REMOVE_SWITCH38,
      EXPERIMENTAL_REMOVE_SWITCH39,
      EXPERIMENTAL_REMOVE_SWITCH40,
      EXPERIMENTAL_REMOVE_SWITCH41,
      EXPERIMENTAL_REMOVE_SWITCH42,
      EXPERIMENTAL_REMOVE_SWITCH43,
      EXPERIMENTAL_REMOVE_SWITCH44,
      EXPERIMENTAL_REMOVE_SWITCH45,
      EXPERIMENTAL_REMOVE_SWITCH46,
      EXPERIMENTAL_REMOVE_SWITCH47,
      EXPERIMENTAL_REMOVE_SWITCH48,
      EXPERIMENTAL_REMOVE_SWITCH49,
      EXPERIMENTAL_REMOVE_SWITCH50,
      EXPERIMENTAL_REMOVE_SWITCH51,
      EXPERIMENTAL_REMOVE_SWITCH52,
      EXPERIMENTAL_REMOVE_SWITCH53,
      EXPERIMENTAL_REMOVE_SWITCH54,
      EXPERIMENTAL_REMOVE_SWITCH55,
      EXPERIMENTAL_REMOVE_SWITCH56,
      EXPERIMENTAL_REMOVE_SWITCH57,
      EXPERIMENTAL_REMOVE_SWITCH58,
      EXPERIMENTAL_REMOVE_SWITCH59,
      EXPERIMENTAL_REMOVE_SWITCH60,
      EXPERIMENTAL_REMOVE_SWITCH61,
      EXPERIMENTAL_REMOVE_SWITCH62,
      EXPERIMENTAL_REMOVE_SWITCH63,
      EXPERIMENTAL_REMOVE_SWITCH64,
      EXPERIMENTAL_REMOVE_SWITCH65,
      EXPERIMENTAL_REMOVE_SWITCH66,
      EXPERIMENTAL_REMOVE_SWITCH67,
      EXPERIMENTAL_REMOVE_SWITCH68,
      EXPERIMENTAL_REMOVE_SWITCH69,
      EXPERIMENTAL_REMOVE_SWITCH70,
      EXPERIMENTAL_REMOVE_SWITCH71,
      EXPERIMENTAL_REMOVE_SWITCH72,
      EXPERIMENTAL_REMOVE_SWITCH73,
      EXPERIMENTAL_REMOVE_SWITCH74,
      EXPERIMENTAL_REMOVE_SWITCH75,
      EXPERIMENTAL_REMOVE_SWITCH76,
      EXPERIMENTAL_REMOVE_SWITCH77,
      EXPERIMENTAL_REMOVE_SWITCH78,
      EXPERIMENTAL_REMOVE_SWITCH79,
      EXPERIMENTAL_REMOVE_SWITCH80,
      EXPERIMENTAL_REMOVE_SWITCH81,
      EXPERIMENTAL_REMOVE_SWITCH82,
      EXPERIMENTAL_REMOVE_SWITCH83,
      EXPERIMENTAL_REMOVE_SWITCH84,
      EXPERIMENTAL_REMOVE_SWITCH85,
      EXPERIMENTAL_REMOVE_SWITCH86,
      EXPERIMENTAL_REMOVE_SWITCH87,
      EXPERIMENTAL_REMOVE_SWITCH88,
      EXPERIMENTAL_REMOVE_SWITCH89,
      EXPERIMENTAL_REMOVE_SWITCH90,
      EXPERIMENTAL_REMOVE_SWITCH91,
      EXPERIMENTAL_REMOVE_SWITCH92,
      EXPERIMENTAL_REMOVE_SWITCH93,
      EXPERIMENTAL_REMOVE_SWITCH94,
      EXPERIMENTAL_REMOVE_SWITCH95,
      EXPERIMENTAL_REMOVE_SWITCH96,
      EXPERIMENTAL_REMOVE_SWITCH97,
      EXPERIMENTAL_REMOVE_SWITCH98,
      EXPERIMENTAL_REMOVE_SWITCH99),

  /**
   * Default mutators
   */
  DEFAULTS(INVERT_NEGS, RETURN_VALS, MATH, VOID_METHOD_CALLS,
      NEGATE_CONDITIONALS, CONDITIONALS_BOUNDARY, INCREMENTS),
      
  STRONGER(DEFAULTS, REMOVE_CONDITIONALS, EXPERIMENTAL_SWITCH),

  /**
   * All the mutators
   */
  ALL(DEFAULTS, NON_VOID_METHOD_CALLS, CONSTRUCTOR_CALLS, INLINE_CONSTS,
      EXPERIMENTAL_MEMBER_VARIABLE, EXPERIMENTAL_SWITCH, REMOVE_CONDITIONALS, REMOVE_INCREMENTS, REMOVE_SWITCH);

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
