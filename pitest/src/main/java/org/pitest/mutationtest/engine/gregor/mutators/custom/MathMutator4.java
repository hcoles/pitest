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

public enum MathMutator4 implements MethodMutatorFactory {

    MATH_MUTATOR4;

    @Override
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new MathMethodVisitor4(this, methodInfo, context, methodVisitor);
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

class MathMethodVisitor4 extends AbstractInsnMutator {

    MathMethodVisitor4(final MethodMutatorFactory factory,
                      final MethodInfo methodInfo, final MutationContext context,
                      final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

    static {
        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.IREM,
                "Substitued  addition with modulus"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.IADD,
                "Substitued  division with addition"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.ISUB,
                "Substitued  multiplication with subtraction"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.IMUL,
                "Substitued  subtraction with multiplication"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.IDIV,
                "Substitued  modulus with divsion"));


        // longs

        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.LREM,
                "Substitued  addition with modulus"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.LADD,
                "Substitued  division with addition"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.LSUB,
                "Substitued  multiplication with subtraction"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.LMUL,
                "Substitued  subtraction with multiplication"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.LDIV,
                "Substitued  modulus with divsion"));


        // floats
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.FREM,
                "Substitued  addition with modulus"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.FADD,
                "Substitued  division with addition"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.FSUB,
                "Substitued  multiplication with subtraction"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.FMUL,
                "Substitued  subtraction with multiplication"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.FDIV,
                "Substitued  modulus with divsion"));

        // doubles
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DREM,
                "Substitued  addition with modulus"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DADD,
                "SubstDtued  division with addition"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DSUB,
                "Substitued  multiplication with subtraction"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DMUL,
                "Substitued  subtraction with multiplication"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DDIV,
                "Substitued  modulus with divsion"));


    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}
