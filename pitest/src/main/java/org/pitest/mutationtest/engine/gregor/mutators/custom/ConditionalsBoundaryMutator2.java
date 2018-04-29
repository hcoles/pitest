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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractJumpMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import java.util.HashMap;
import java.util.Map;

public enum ConditionalsBoundaryMutator2 implements MethodMutatorFactory {

  CONDITIONALS_BOUNDARY_MUTATOR2;

  @Override
  public MethodVisitor create(final MutationContext context,
                              final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ConditionalsBoundaryMethodVisitor2(this, context, methodVisitor);
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

class ConditionalsBoundaryMethodVisitor2 extends AbstractJumpMutator {

  private static final String                     DESCRIPTION = "changed conditional boundary";
  private static final Map<Integer, Substitution> MUTATIONS   = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IFLE, new Substitution(Opcodes.IFLT, "CHANGED LE TO LT"));
    MUTATIONS.put(Opcodes.IFGE, new Substitution(Opcodes.IFEQ, "CHANGED GE TO EQ"));
    MUTATIONS.put(Opcodes.IFGT, new Substitution(Opcodes.IFNE, "CHANGED GT TO NE"));
    MUTATIONS.put(Opcodes.IFLT, new Substitution(Opcodes.IFLE, "CHANGED LT TO LE"));
    MUTATIONS.put(Opcodes.IFEQ, new Substitution(Opcodes.IFGE, "CHANGED EQ TO GE"));
    MUTATIONS.put(Opcodes.IFNE, new Substitution(Opcodes.IFGT, "CHANGED NE TO GT"));

    MUTATIONS.put(Opcodes.IF_ICMPLE, new Substitution(Opcodes.IF_ICMPLT, "CHANGED LE TO LT"));
    MUTATIONS.put(Opcodes.IF_ICMPGE, new Substitution(Opcodes.IF_ICMPEQ, "CHANGED LE TO EQ"));
    MUTATIONS.put(Opcodes.IF_ICMPGT, new Substitution(Opcodes.IF_ICMPNE, "CHANGED LE TO NE"));
    MUTATIONS.put(Opcodes.IF_ICMPLT, new Substitution(Opcodes.IF_ICMPLE, "CHANGED LE TO LE"));
    MUTATIONS.put(Opcodes.IF_ICMPEQ, new Substitution(Opcodes.IF_ICMPGE, "CHANGED LE TO GE"));
    MUTATIONS.put(Opcodes.IF_ICMPNE, new Substitution(Opcodes.IF_ICMPGT, "CHANGED LE TO GT"));

  }

  ConditionalsBoundaryMethodVisitor2(final MethodMutatorFactory factory,
                                    final MutationContext context, final MethodVisitor delegateMethodVisitor) {
    super(factory, context, delegateMethodVisitor);
  }

  @Override
  protected Map<Integer, Substitution> getMutations() {
    return MUTATIONS;
  }

}
