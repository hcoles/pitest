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

import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ConstructorCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.IncrementsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InlineConstantMutator;
import org.pitest.mutationtest.engine.gregor.mutators.InvertNegsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NegateConditionalsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.NonVoidMethodCallMutator;
import org.pitest.mutationtest.engine.gregor.mutators.ReturnValsMutator;
import org.pitest.mutationtest.engine.gregor.mutators.VoidMethodCallMutator;

public enum Mutator implements MethodMutatorFactory {

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
  INLINE_CONSTS(InlineConstantMutator.INLINE_CONSTANT_MUTATOR),

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
   * Optional mutator that removes method calls to non void methods.
   */
  NON_VOID_METHOD_CALLS(NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR),

  /**
   * Optional mutator that replaces constructor calls with null values.
   */
  CONSTRUCTOR_CALLS(ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR),

  /**
   * Experimental mutator that mutates integer and floating point inline
   * constants.
   */
  EXPERIMENTAL_INLINE_CONSTS(
      new org.pitest.mutationtest.engine.gregor.mutators.experimental.InlineConstantMutator());

  Mutator(final MethodMutatorFactory impl) {
    this.impl = impl;
  }

  private final MethodMutatorFactory impl;

  public String getName() {
    return this.impl.getName();
  }

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return this.impl.create(context, methodInfo, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.impl.getGloballyUniqueId();
  }
}
