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
import org.pitest.mutationtest.engine.MutationIdentifier;

public abstract class JumpMutator extends LineTrackingMethodAdapter {

  private final MethodMutatorFactory factory;

  public static class Substitution {
    public Substitution(final int newCode, final String description) {
      this.newCode = newCode;
      this.description = description;
    }

    int    newCode;
    String description;
  }

  public JumpMutator(final MethodInfo methodInfo, final Context context,
      final MethodVisitor writer, final MethodMutatorFactory factory) {
    super(methodInfo, context, writer);
    this.factory = factory;
  }

  protected abstract Map<Integer, Substitution> getMutations();

  @Override
  public void visitJumpInsn(final int opcode, final Label label) {
    if (this.getMutations().containsKey(opcode)) {
      createMutation(opcode, this.getMutations().get(opcode), label);
    } else {
      this.mv.visitJumpInsn(opcode, label);
    }

  }

  private void createMutation(final int opcode,
      final Substitution substitution, final Label label) {
    final MutationIdentifier newId = this.context.registerMutation(
        this.factory, substitution.description);
    if (this.context.shouldMutate(newId)) {
      this.mv.visitJumpInsn(substitution.newCode, label);
    } else {
      this.mv.visitJumpInsn(opcode, label);
    }
  }

}
