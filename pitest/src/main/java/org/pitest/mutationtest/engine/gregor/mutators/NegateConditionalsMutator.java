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
package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum NegateConditionalsMutator implements MethodMutatorFactory {

  NEGATE_CONDITIONALS_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ConditionalMethodVisitor(this, context, methodVisitor);
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

class ConditionalMethodVisitor extends AbstractJumpMutator {

  private static final String                     DESCRIPTION = "negated conditional";
  private static final Map<Integer, Substitution> MUTATIONS   = new HashMap<Integer, Substitution>();

  static {
    MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFNE, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFEQ, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFGT, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFLT, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFLE, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFGE, DESCRIPTION));
    MUTATIONS.put(Opcodes.IFNULL, new Substitution(Opcodes.IFNONNULL,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IFNONNULL, new Substitution(Opcodes.IFNULL,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPEQ,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPNE,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPGT,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPLT,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPLE,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPGE,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ACMPEQ, new Substitution(Opcodes.IF_ACMPNE,
        DESCRIPTION));
    MUTATIONS.put(Opcodes.IF_ACMPNE, new Substitution(Opcodes.IF_ACMPEQ,
        DESCRIPTION));
  }

  ConditionalMethodVisitor(final MethodMutatorFactory factory,
      final MutationContext context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return MUTATIONS;
  }

}
