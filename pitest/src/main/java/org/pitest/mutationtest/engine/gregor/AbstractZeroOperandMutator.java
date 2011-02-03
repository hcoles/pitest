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

import org.objectweb.asm.MethodVisitor;
import org.pitest.mutationtest.engine.MutationIdentifier;

public abstract class AbstractZeroOperandMutator extends
    LineTrackingMethodAdapter {

  protected final Class<?> mutatorType;

  public AbstractZeroOperandMutator(final Class<?> mutatorType,
      final MethodInfo methodInfo, final Context context,
      final MethodVisitor writer) {
    super(methodInfo, context, writer);
    this.mutatorType = mutatorType;
  }

  @Override
  public void visitInsn(final int opcode) {
    if (canMutate(opcode)) {
      createMutation(opcode);
    } else {
      this.mv.visitInsn(opcode);
    }

  }

  private boolean canMutate(final int opcode) {
    return getMutations().containsKey(opcode);
  }

  protected abstract Map<Integer, ZeroOperandMutation> getMutations();

  protected abstract void applyUnmutatedInstruction(final int opcode);

  private void createMutation(final int opcode) {
    final ZeroOperandMutation mutation = getMutations().get(opcode);
    final MutationIdentifier newId = this.context.registerMutation(
        this.mutatorType, mutation.decribe(opcode));
    if (this.context.shouldMutate(newId)) {
      mutation.apply(opcode, this.mv);
    } else {
      applyUnmutatedInstruction(opcode);
    }
  }

}
