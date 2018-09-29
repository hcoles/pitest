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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * The <code>MemberVariableMutator</code> is a mutator that mutates assignments
 * to member variables by removing them.
 *
 * @author Stefan Penndorf &lt;stefan.penndorf@gmail.com&gt;
 */
public class MemberVariableMutator implements MethodMutatorFactory {

  private final class MemberVariableVisitor extends MethodVisitor {

    private final MutationContext context;

    MemberVariableVisitor(final MutationContext context,
        final MethodVisitor delegateVisitor) {
      super(ASMVersion.ASM_VERSION, delegateVisitor);
      this.context = context;
    }

    @Override
    public void visitFieldInsn(final int opcode, final String owner,
        final String name, final String desc) {
      if ((Opcodes.PUTFIELD == opcode) && shouldMutate(name)) {
        // removed setting field

        // pop the values which PUTFIELD would have used
        if (Type.getType(desc).getSize() == 2) {
          super.visitInsn(Opcodes.POP2);
          super.visitInsn(Opcodes.POP);
        } else {
          super.visitInsn(Opcodes.POP);
          super.visitInsn(Opcodes.POP);
        }
      } else {
        super.visitFieldInsn(opcode, owner, name, desc);
      }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodAdapter#visitMethodInsn(int,
     * java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public void visitMethodInsn(final int opcode, final String owner,
        final String name, final String desc, boolean itf) {
      super.visitMethodInsn(opcode, owner, name, desc, itf);
    }

    private boolean shouldMutate(final String fieldName) {
      final MutationIdentifier mutationId = this.context.registerMutation(
          MemberVariableMutator.this, "Removed assignment to member variable "
              + fieldName);
      return this.context.shouldMutate(mutationId);
    }

  }

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new MemberVariableVisitor(context, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String toString() {
    return "EXPERIMENTAL_MEMBER_VARIABLE_MUTATOR";
  }

  @Override
  public String getName() {
    return toString();
  }

}
