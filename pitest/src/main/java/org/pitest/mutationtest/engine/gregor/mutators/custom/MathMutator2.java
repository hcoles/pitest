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

public enum MathMutator2 implements MethodMutatorFactory {

    MATH_MUTATOR2;

    @Override
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new MathMethodVisitor2(this, methodInfo, context, methodVisitor);
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

class MathMethodVisitor2 extends AbstractInsnMutator {

    MathMethodVisitor2(final MethodMutatorFactory factory,
                      final MethodInfo methodInfo, final MutationContext context,
                      final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

    static {
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IMUL,
                "Substitued  addition with multiplication"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IDIV,
                "Substitued  division with Divsion"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IREM,
                "Substitued  multiplication with modulus"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IADD,
                "Substitued  subtraction with addition"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.ISUB,
                "Substitued  modulus with subtraction"));


        // longs

        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LMUL,
                "Substitued  addition with multiplication"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LDIV,
                "Substitued  division with Divsion"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LREM,
                "Substitued  multiplication with modulus"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LADD,
                "Substitued  subtraction with addition"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LSUB,
                "Substitued  modulus with subtraction"));


        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FMUL,
                "Substitued addition with multiplication"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FDIV,
                "Substitued  division with Divsion"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FREM,
                "Substitued  multiplication with modulus"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FADD,
                "Substitued  subtraction with addition"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FSUB,
                "Substitued  modulus with subtraction"));


        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DMUL,
                "Substitued  addition with multiplication"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DDIV,
                "Substitued  division with Divsion"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DREM,
                "Substitued  multiplication with modulus"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DADD,
                "Substitued  subtraction with addition"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DSUB,
                "Substitued  modulus with subtraction"));


    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}
