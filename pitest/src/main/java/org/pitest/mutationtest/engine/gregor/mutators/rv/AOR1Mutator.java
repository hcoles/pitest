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

public enum AOR1Mutator implements MethodMutatorFactory {

  AOR_1_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new AOR1MethodVisitor(this, methodInfo, context, methodVisitor);
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

class AOR1MethodVisitor extends AbstractInsnMutator {

  AOR1MethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.ISUB,
        "Replaced integer addition with subtraction"));
    MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IADD,
        "Replaced integer subtraction with addition"));
    MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IDIV,
        "Replaced integer multiplication with division"));
    MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IMUL,
        "Replaced integer division with multiplication"));
    MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IMUL,
        "Replaced integer modulus with multiplication"));

    // longs
    MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LSUB,
        "Replaced long addition with subtraction"));
    MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LADD,
        "Replaced long subtraction with addition"));
    MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LDIV,
        "Replaced long multiplication with division"));
    MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LMUL,
        "Replaced long division with multiplication"));
    MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LMUL,
        "Replaced long modulus with multiplication"));

    // floats
    MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FSUB,
        "Replaced float addition with subtraction"));
    MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FADD,
        "Replaced float subtraction with addition"));
    MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FDIV,
        "Replaced float multiplication with division"));
    MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FMUL,
        "Replaced float division with multiplication"));
    MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FMUL,
        "Replaced float modulus with multiplication"));

    // doubles
    MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DSUB,
        "Replaced double addition with subtraction"));
    MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DADD,
        "Replaced double subtraction with addition"));
    MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DDIV,
        "Replaced double multiplication with division"));
    MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DMUL,
        "Replaced double division with multiplication"));
    MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DMUL,
        "Replaced double modulus with multiplication"));

  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }

}
