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
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.InsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

public enum InlineConstantMutator implements MethodMutatorFactory {

  INLINE_CONSTANT_MUTATOR;

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new InlineConstantMethodVisitor(this.getClass(), methodInfo,
        context, methodVisitor);
  }

}

class InlineConstantMethodVisitor extends InsnMutator {

  private final static Map<Integer, ZeroOperandMutation> mutations = new HashMap<Integer, ZeroOperandMutation>();

  static {
    mutations.put(Opcodes.ICONST_M1, new InsnSubstitution(Opcodes.ICONST_1,
        "Substituted -1 with 1"));
    mutations.put(Opcodes.ICONST_0, new InsnSubstitution(Opcodes.ICONST_1,
        "Substituted 0 with 1"));
    mutations.put(Opcodes.ICONST_1, new InsnSubstitution(Opcodes.ICONST_0,
        "Substituted 1 with 0"));
    mutations.put(Opcodes.ICONST_2, new InsnSubstitution(Opcodes.ICONST_3,
        "Substituted 2 with 3"));
    mutations.put(Opcodes.ICONST_3, new InsnSubstitution(Opcodes.ICONST_4,
        "Substituted 3 with 4"));
    mutations.put(Opcodes.ICONST_4, new InsnSubstitution(Opcodes.ICONST_5,
        "Substituted 4 with 5"));
    mutations.put(Opcodes.ICONST_5, new InsnSubstitution(Opcodes.ICONST_M1,
        "Substituted 5 with -1"));
    mutations.put(Opcodes.FCONST_0, new InsnSubstitution(Opcodes.FCONST_1,
        "Substituted 0.0 with 1.0"));
    mutations.put(Opcodes.FCONST_1, new InsnSubstitution(Opcodes.FCONST_0,
        "Substituted 1.0 with 0.0"));
    mutations.put(Opcodes.FCONST_2, new InsnSubstitution(Opcodes.FCONST_0,
        "Substituted 2.0 with 0.0"));
    mutations.put(Opcodes.DCONST_0, new InsnSubstitution(Opcodes.DCONST_1,
        "Substituted 0.0 with 1.0"));
    mutations.put(Opcodes.DCONST_1, new InsnSubstitution(Opcodes.DCONST_0,
        "Substituted 1.0 with 0.0"));
    mutations.put(Opcodes.LCONST_0, new InsnSubstitution(Opcodes.LCONST_1,
        "Substituted 0 with 1"));
    mutations.put(Opcodes.LCONST_1, new InsnSubstitution(Opcodes.LCONST_0,
        "Substituted 1 with 0.0"));

  }

  public InlineConstantMethodVisitor(final Class<?> mutatorType,
      final MethodInfo methodInfo, final Context context,
      final MethodVisitor writer) {
    super(mutatorType, methodInfo, context, writer);
  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return mutations;
  }

  @Override
  public void visitIntInsn(final int opcode, final int var) {

    if ((opcode == Opcodes.BIPUSH) || (opcode == Opcodes.SIPUSH)) {
      createBiPushMutation(opcode, var);
    } else {
      this.mv.visitIntInsn(opcode, var);
    }

  }

  @Override
  public void visitLdcInsn(final Object cst) {
    final Object sub = getLdcSubstitution(cst);
    if (!sub.equals(cst)) {
      final MutationIdentifier newId = this.context.registerMutation(
          this.mutatorType, "Replaced constant value of " + cst + " with "
              + sub);
      if (this.context.shouldMutate(newId)) {
        this.mv.visitLdcInsn(sub);
      } else {
        this.mv.visitLdcInsn(cst);
      }
    } else {
      this.mv.visitLdcInsn(cst);
    }
  }

  private void createBiPushMutation(final int opcode, final int var) {
    final MutationIdentifier newId = this.context.registerMutation(
        this.mutatorType, "Replaced constant value of " + var + " with "
            + (var + 1));
    if (this.context.shouldMutate(newId)) {
      this.mv.visitIntInsn(opcode, (var + 1));
    } else {
      this.mv.visitIntInsn(opcode, var);
    }
  }

  private Object getLdcSubstitution(final Object o) {
    // do not mutate strings or .class here
    // avoid addition to floating points as may yield
    // same value
    if (o instanceof Integer) {
      return ((Integer) o) + 1;
    } else if (o instanceof Long) {
      return ((Long) o) + 1;
    } else if (o instanceof Float) {
      // will the compiler ever generate 1f for ldc?
      return 1f;
    } else if (o instanceof Double) {
      return 1d;
    } else {
      return o;
    }

  }

}
