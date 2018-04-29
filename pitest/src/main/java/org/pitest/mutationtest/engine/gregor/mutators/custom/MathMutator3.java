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

public enum MathMutator3 implements MethodMutatorFactory {

    MATH_MUTATOR3;

    @Override
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new MathMethodVisitor3(this, methodInfo, context, methodVisitor);
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

class MathMethodVisitor3 extends AbstractInsnMutator {

    MathMethodVisitor3(final MethodMutatorFactory factory,
                      final MethodInfo methodInfo, final MutationContext context,
                      final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

    static {
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IDIV,
                "Substitued  addition with divsion"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IREM,
                "Substitued  division with modulus"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.IADD,
                "Substitued  multiplication with addition"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.ISUB,
                "Substitued  subtraction with subtraction"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IMUL,
                "Substitued  modulus with multiplication"));


        // longs

        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LDIV,
                "Substitued  addition with divsion"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LREM,
                "Substitued  division with modulus"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LADD,
                "Substitued  multiplication with addition"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LSUB,
                "Substitued  subtraction with subtraction"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LMUL,
                "Substitued  modulus with multiplication"));


        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FDIV,
                "Substitued  addition with divsion"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FREM,
                "Substitued  division with modulus"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FADD,
                "Substitued  multiplication with addition"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FSUB,
                "Substitued  subtraction with subtraction"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FMUL,
                "Substitued  modulus with multiplication"));


        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DDIV,
                "Substitued  addition with divsion"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DREM,
                "Substitued  division with modulus"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DADD,
                "Substitued  multiplication with addition"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DSUB,
                "Substitued  subtraction with subtraction"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DMUL,
                "Substitued  modulus with multiplication"));


    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}
