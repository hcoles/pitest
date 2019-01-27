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
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

import java.util.HashMap;
import java.util.Map;

public enum AOR2Mutator implements MethodMutatorFactory {

  AOR_2_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new AOR2MethodVisitor(this, methodInfo, context, methodVisitor);
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

class AOR2MethodVisitor extends AbstractInsnMutator {

  AOR2MethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IMUL,
        "Replaced integer addition with multiplication"));
    MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IMUL,
        "Replaced integer subtraction with multiplication"));
    MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IREM,
        "Replaced integer multiplication with modulus"));
    MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IREM,
        "Replaced integer division with modulus"));
    MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IDIV,
        "Replaced integer modulus with division"));

    // longs
    MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LMUL,
        "Replaced long addition with multiplication"));
    MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LMUL,
        "Replaced long subtraction with multiplication"));
    MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LREM,
        "Replaced long multiplication with modulus"));
    MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LREM,
        "Replaced long division with modulus"));
    MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LDIV,
        "Replaced long modulus with division"));

    // floats
    MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FMUL,
        "Replaced float addition with multiplication"));
    MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FMUL,
        "Replaced float subtraction with multiplication"));
    MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FREM,
        "Replaced float multiplication with modulus"));
    MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FREM,
        "Replaced float division with modulus"));
    MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FDIV,
        "Replaced float modulus with division"));

    // doubles
    MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DMUL,
        "Replaced double addition with multiplication"));
    MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DMUL,
        "Replaced double subtraction with multiplication"));
    MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DREM,
        "Replaced double multiplication with modulus"));
    MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DREM,
        "Replaced double division with modulus"));
    MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DDIV,
        "Replaced double modulus with division"));

  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }

}
