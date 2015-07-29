/*
 * Copyright 2014 Stefan Mandel, Urs Metz
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

package org.pitest.mutationtest.engine.gregor.mutators;

import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator for non-void methods that have a parameter that matches the return
 * type: it replaces the result of the method call with a parameter. E. g. the
 * method call
 *
 * <pre>
 * public int originalMethod() {
 *   int someInt = 3;
 *   return someOtherMethod(someInt);
 * }
 * 
 * private int someOtherMethod(int parameter) {
 *   return parameter + 1;
 * }
 * </pre>
 *
 * is mutated to
 *
 * <pre>
 * public int mutatedMethod() {
 *   int someInt = 3;
 *   return someInt;
 * }
 * </pre>
 */
public enum ArgumentPropagationMutator implements MethodMutatorFactory {

  ARGUMENT_PROPAGATION_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ArgumentPropagationVisitor(context, methodVisitor, this);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return name();
  }

}
