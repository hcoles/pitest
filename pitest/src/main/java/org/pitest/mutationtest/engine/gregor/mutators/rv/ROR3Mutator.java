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

public enum ROR3Mutator implements MethodMutatorFactory {

  ROR_3_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ROR3MethodVisitor(this, context, methodVisitor);
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

class ROR3MethodVisitor extends AbstractJumpMutator {

  private static final Map<Integer, Substitution> MUTATIONS   = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFGE, "Less than to greater or equal"));
    MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPGE,
            "Less than to greater or equal"));
    MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFGE, "Less or equal to greater or equal"));
    MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPGE,
            "Less or equal to greater or equal"));
    MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFGE, "greater than to greater or equal"));
    MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPGE,
            "greater than to greater or equal"));
    MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFGT, "greater or equal to greater than"));
    MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPGT,
            "greater or equal to greater than"));
    MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFGT, "equal to greater than"));
    MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPGT,
            "equal to greater than"));
    MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFGT, "not equal to greater than"));
    MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPGT,
            "not equal to greater than"));
  }

  ROR3MethodVisitor(final MethodMutatorFactory factory,
                    final MutationContext context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return MUTATIONS;
  }

}
