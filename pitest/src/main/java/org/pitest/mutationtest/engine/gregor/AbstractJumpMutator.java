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
package org.pitest.mutationtest.engine.gregor;

import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;

public abstract class AbstractJumpMutator extends MethodVisitor {

  private final MethodMutatorFactory factory;
  private final MutationContext      context;

  public static class Substitution {
    public Substitution(final int newCode, final String description) {
      this.newCode = newCode;
      this.description = description;
    }

    private final int    newCode;
    private final String description;
  }

  public AbstractJumpMutator(final MethodMutatorFactory factory,
      final MutationContext context, final MethodVisitor writer) {
    super(ASMVersion.ASM_VERSION, writer);
    this.factory = factory;
    this.context = context;
  }

  protected abstract Map<Integer, Substitution> getMutations();

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    if (canMutate(opcode)) {
      createMutationForJumpInsn(opcode, label);
    } else {
      this.mv.visitJumpInsn(opcode, label);
    }
  }

  private boolean canMutate(final int opcode) {
    return this.getMutations().containsKey(opcode);
  }

  private void createMutationForJumpInsn(final int opcode, final Label label) {
    final Substitution substitution = this.getMutations().get(opcode);

    final MutationIdentifier newId = this.context.registerMutation(
        this.factory, substitution.description);

    if (this.context.shouldMutate(newId)) {
      this.mv.visitJumpInsn(substitution.newCode, label);
    } else {
      this.mv.visitJumpInsn(opcode, label);
    }
  }

}
