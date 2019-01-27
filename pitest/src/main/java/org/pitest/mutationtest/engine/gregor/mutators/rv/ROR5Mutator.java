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
package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import java.util.HashMap;
import java.util.Map;

public enum ROR5Mutator implements MethodMutatorFactory {

  ROR_5_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ROR5MethodVisitor(this, context, methodVisitor);
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

class ROR5MethodVisitor extends AbstractJumpMutator {

  private static final Map<Integer, Substitution> MUTATIONS   = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFNE, "Less than to not equal"));
    MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPNE,
            "Less than to not equal"));
    MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFNE, "Less or equal to not equal"));
    MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPNE,
            "Less or equal to not equal"));
    MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFNE, "greater than to not equal"));
    MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPNE,
            "greater than to not equal"));
    MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFNE, "greater or equal to not equal"));
    MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPNE,
            "greater or equal to not equal"));
    MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFNE, "equal to not equal"));
    MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPNE,
            "equal to not equal"));
    MUTATIONS.put(Opcodes.IFNULL, new Substitution(Opcodes.IFNONNULL,
            "equal to not equal"));
    MUTATIONS.put(Opcodes.IF_ACMPEQ, new Substitution(Opcodes.IF_ACMPNE,
            "equal to not equal"));
    MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFEQ, "not equal to equal"));
    MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPEQ,
            "not equal to equal"));
    MUTATIONS.put(Opcodes.IFNONNULL, new Substitution(Opcodes.IFNULL,
            "not equal to equal"));
    MUTATIONS.put(Opcodes.IF_ACMPNE, new Substitution(Opcodes.IF_ACMPEQ,
            "not equal to equal"));
  }

  ROR5MethodVisitor(final MethodMutatorFactory factory,
                    final MutationContext context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return MUTATIONS;
  }

}
