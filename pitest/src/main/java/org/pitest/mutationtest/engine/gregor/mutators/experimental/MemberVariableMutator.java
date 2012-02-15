/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
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
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

/**
 * The <code>MemberVariableMutator</code> is a mutator that mutates assignments
 * to member variables by removing them.
 * 
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class MemberVariableMutator implements MethodMutatorFactory {

  private final class MemberVariableVisitor extends MethodAdapter {

    private final Context context;

    public MemberVariableVisitor(final Context context,
        final MethodVisitor delegateVisitor) {
      super(delegateVisitor);
      this.context = context;
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
        final String name, final String desc) {
      if (Opcodes.PUTFIELD == opcode && shouldMutate(name)) {
        // removed setting field
      } else {
        super.visitFieldInsn(opcode, owner, name, desc);
      }
    }
    
    /* (non-Javadoc)
     * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
      super.visitMethodInsn(opcode, owner, name, desc);
    }

    private boolean shouldMutate(final String fieldName) {
      final MutationIdentifier mutationId = this.context.registerMutation(
          MemberVariableMutator.this, "Removed assignment to member variable "
              + fieldName);
      return this.context.shouldMutate(mutationId);
    }

  }

  public MethodVisitor create(Context context, MethodInfo methodInfo,
      MethodVisitor methodVisitor) {
    return new MemberVariableVisitor(context, methodVisitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String toString() {
    return "EXPERIMENTAL_MEMBER_VARIABLE_MUTATOR";
  }

  public String getName() {
    return toString();
  }

}
