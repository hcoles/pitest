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
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

public enum MathMutator1 implements MethodMutatorFactory {

    MATH_MUTATOR1;

    @Override
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new MathMethodVisitor1(this, methodInfo, context, methodVisitor);
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

class MathMethodVisitor1 extends AbstractInsnMutator {

    MathMethodVisitor1(final MethodMutatorFactory factory,
                      final MethodInfo methodInfo, final MutationContext context,
                      final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

    static {
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.ISUB,
                "Substituted addition with Subtraction"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IMUL,
                "Substituted subtraction with Multiplication"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IDIV,
                "Substituted multiplication with division"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IREM,
                "Substituted division with modulus"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IADD,
                "Substituted modulus with addition"));


        // longs

        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LSUB,
                "Substituted addition with Subtraction"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LMUL,
                "Substituted  subtraction with Multiplication"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LDIV,
                "Substituted multiplication with division"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LREM,
                "Substituted division with modulus"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LADD,
                "Substituted modulus with addition"));


        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FSUB,
                "Substituted addition with Subtraction"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FMUL,
                "Substituted subtraction with Multiplication"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FDIV,
                "Substituted multiplication with division"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FREM,
                "Substituted division with modulus"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FADD,
                "Substituted modulus with addition"));

        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DSUB,
                "Substituted addition with Subtraction"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DMUL,
                "Substituted subtraction with Multiplication"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DDIV,
                "Substituted multiplication with division"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DREM,
                "Substituted division with modulus"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DADD,
                "Substituted modulus with addition"));

    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}
