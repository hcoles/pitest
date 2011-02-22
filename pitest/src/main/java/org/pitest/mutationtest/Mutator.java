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

  NEGS(InvertNegsMutator.INVERT_NEGS_MUTATOR), RETURN_VALS(
      ReturnValsMutator.RETURN_VALS_MUTATOR), INLINE_CONSTS(
      InlineConstantMutator.INLINE_CONSTANT_MUTATOR), MATH(
      MathMutator.MATH_MUTATOR), VOID_METHOD_CALLS(
      VoidMethodCallMutator.VOID_METHOD_CALL_MUTATOR), NEGATE_CONDITIONALS(
      NegateConditionalsMutator.NEGATE_CONDITIONALS_MUTATOR), CONDITIONALS_BOUNDARY_MUTATOR(
      ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR), INCREMENTS(
      IncrementsMutator.INCREMENTS_MUTATOR), NON_VOID_METHOD_CALLS(
      NonVoidMethodCallMutator.NON_VOID_METHOD_CALL_MUTATOR), CONSTRUCTOR_CALLS(
      ConstructorCallMutator.CONSTRUCTOR_CALL_MUTATOR);

  Mutator(final MethodMutatorFactory impl) {
    this.impl = impl;
  }

  private final MethodMutatorFactory impl;

  @Override
  public String toString() {
    return this.impl.toString();
  }

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return this.impl.create(context, methodInfo, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.impl.getGloballyUniqueId();
  }
}
