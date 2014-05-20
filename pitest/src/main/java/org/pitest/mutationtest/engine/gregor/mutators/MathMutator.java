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
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

public enum MathMutator implements MethodMutatorFactory {

  MATH_MUTATOR;

  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new MathMethodVisitor(this, methodInfo, context, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  public String getName() {
    return name();
  }

}

class MathMethodVisitor extends AbstractInsnMutator {

  public MathMethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private final static Map<Integer, ZeroOperandMutation> mutations = new HashMap<Integer, ZeroOperandMutation>();

  static {
    mutations.put(Opcodes.IADD, new InsnSubstitution(Opcodes.ISUB,
        "Replaced integer addition with subtraction"));
    mutations.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IADD,
        "Replaced integer subtraction with addition"));
    mutations.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IDIV,
        "Replaced integer multiplication with division"));
    mutations.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IMUL,
        "Replaced integer division with multiplication"));
    mutations.put(Opcodes.IOR, new InsnSubstitution(Opcodes.IAND,
        "Replaced bitwise OR with AND"));
    mutations.put(Opcodes.IAND, new InsnSubstitution(Opcodes.IOR,
        "Replaced bitwise AND with OR"));
    mutations.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IMUL,
        "Replaced integer modulus with multiplication"));
    mutations.put(Opcodes.IXOR, new InsnSubstitution(Opcodes.IAND,
        "Replaced XOR with AND"));
    mutations.put(Opcodes.ISHL, new InsnSubstitution(Opcodes.ISHR,
        "Replaced Shift Left with Shift Right"));
    mutations.put(Opcodes.ISHR, new InsnSubstitution(Opcodes.ISHL,
        "Replaced Shift Right with Shift Left"));
    mutations.put(Opcodes.IUSHR, new InsnSubstitution(Opcodes.ISHL,
        "Replaced Unsigned Shift Right with Shift Left"));

    // longs

    mutations.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LSUB,
        "Replaced long addition with subtraction"));
    mutations.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LADD,
        "Replaced long subtraction with addition"));
    mutations.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LDIV,
        "Replaced long multiplication with division"));
    mutations.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LMUL,
        "Replaced long division with multiplication"));
    mutations.put(Opcodes.LOR, new InsnSubstitution(Opcodes.LAND,
        "Replaced bitwise OR with AND"));
    mutations.put(Opcodes.LAND, new InsnSubstitution(Opcodes.LOR,
        "Replaced bitwise AND with OR"));
    mutations.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LMUL,
        "Replaced long modulus with multiplication"));
    mutations.put(Opcodes.LXOR, new InsnSubstitution(Opcodes.LAND,
        "Replaced XOR with AND"));
    mutations.put(Opcodes.LSHL, new InsnSubstitution(Opcodes.LSHR,
        "Replaced Shift Left with Shift Right"));
    mutations.put(Opcodes.LSHR, new InsnSubstitution(Opcodes.LSHL,
        "Replaced Shift Right with Shift Left"));
    mutations.put(Opcodes.LUSHR, new InsnSubstitution(Opcodes.LSHL,
        "Replaced Unsigned Shift Right with Shift Left"));

    // floats
    mutations.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FSUB,
        "Replaced float addition with subtraction"));
    mutations.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FADD,
        "Replaced float subtraction with addition"));
    mutations.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FDIV,
        "Replaced float multiplication with division"));
    mutations.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FMUL,
        "Replaced float division with multiplication"));
    mutations.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FMUL,
        "Replaced float modulus with multiplication"));

    // doubles
    mutations.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DSUB,
        "Replaced double addition with subtraction"));
    mutations.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DADD,
        "Replaced double subtraction with addition"));
    mutations.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DDIV,
        "Replaced double multiplication with division"));
    mutations.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DMUL,
        "Replaced double division with multiplication"));
    mutations.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DMUL,
        "Replaced double modulus with multiplication"));

  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return mutations;
  }

}
