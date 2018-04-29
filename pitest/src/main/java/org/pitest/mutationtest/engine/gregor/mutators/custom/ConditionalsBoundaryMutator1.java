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
package org.pitest.mutationtest.engine.gregor.mutators.custom;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum ConditionalsBoundaryMutator1 implements MethodMutatorFactory {

  CONDITIONALS_BOUNDARY_MUTATOR1;

  @Override
  public MethodVisitor create(final MutationContext context,
                              final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ConditionalsBoundaryMethodVisitor1(this, context, methodVisitor);
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

class ConditionalsBoundaryMethodVisitor1 extends AbstractJumpMutator {

  private static final String                     DESCRIPTION = "changed conditional boundary";
  private static final Map<Integer, Substitution> MUTATIONS   = new HashMap<>();

  static {
      MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFGT, "CHANGED LE TO GT"));
      MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFLT, "CHANGED GE TO LT"));
      MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFEQ, "CHANGED GT TO EQ"));
      MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFNE, "CHANGED LT TO NE"));
      MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFLE, "CHANGED EQ TO LE"));
      MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFGE, "CHANGED NE TO GE"));

      MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPGT, "CHANGED LE TO GT"));
      MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPLT, "CHANGED GE TO LT"));
      MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPEQ, "CHANGED GT TO EQ"));
      MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPNE, "CHANGED LT TO NE"));
      MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPLE, "CHANGED EQ TO LE"));
      MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPGE, "CHANGED NE TO GE"));
  }

      ConditionalsBoundaryMethodVisitor1(final MethodMutatorFactory factory,
                                    final MutationContext context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return MUTATIONS;
  }

}
