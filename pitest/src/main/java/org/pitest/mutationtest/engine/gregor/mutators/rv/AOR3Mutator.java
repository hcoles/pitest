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

public enum AOR3Mutator implements MethodMutatorFactory {

  AOR_3_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new AOR3MethodVisitor(this, methodInfo, context, methodVisitor);
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

class AOR3MethodVisitor extends AbstractInsnMutator {

  AOR3MethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {
    MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IDIV,
        "Replaced integer addition with division"));
    MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IDIV,
        "Replaced integer subtraction with division"));
    MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IADD,
        "Replaced integer multiplication with addition"));
    MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IADD,
        "Replaced integer division with addition"));
    MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IADD,
        "Replaced integer modulus with addition"));

    // longs
    MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LDIV,
        "Replaced long addition with division"));
    MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LDIV,
        "Replaced long subtraction with division"));
    MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LADD,
        "Replaced long multiplication with addition"));
    MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LADD,
        "Replaced long division with addition"));
    MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LADD,
        "Replaced long modulus with addition"));

    // floats
    MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FDIV,
        "Replaced float addition with division"));
    MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FDIV,
        "Replaced float subtraction with division"));
    MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FADD,
        "Replaced float multiplication with addition"));
    MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FADD,
        "Replaced float division with addition"));
    MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FADD,
        "Replaced float modulus with addition"));

    // doubles
    MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DDIV,
        "Replaced double addition with division"));
    MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DDIV,
        "Replaced double subtraction with division"));
    MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DADD,
        "Replaced double multiplication with addition"));
    MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DADD,
        "Replaced double division with addition"));
    MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DADD,
        "Replaced double modulus with addition"));

  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }

}
