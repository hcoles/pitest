/*
 * Copyright 2012 Henry Coles
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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Disables mutations within java assertions
 *
 */
public class AvoidAssertsMethodAdapter extends MethodVisitor {

  private static final String   DISABLE_REASON = "ASSERTS";

  private final MutationContext context;
  private boolean               assertBlockStarted;
  private Label                 destination;

  public AvoidAssertsMethodAdapter(final MutationContext context,
      final MethodVisitor delegateMethodVisitor) {
    super(Opcodes.ASM6, delegateMethodVisitor);
    this.context = context;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {

    if ((opcode == Opcodes.INVOKEVIRTUAL) && "java/lang/Class".equals(owner)
        && "desiredAssertionStatus".equals(name)) {
      this.context.disableMutations(DISABLE_REASON);
    }
    super.visitMethodInsn(opcode, owner, name, desc, itf);
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner,
      final String name, final String desc) {

    if ("$assertionsDisabled".equals(name)) {
      if (opcode == Opcodes.GETSTATIC) {
        this.context.disableMutations(DISABLE_REASON);
        this.assertBlockStarted = true;
      } else if (opcode == Opcodes.PUTSTATIC) {
        this.context.enableMutatations(DISABLE_REASON);
      }
    }
    super.visitFieldInsn(opcode, owner, name, desc);

  }

  @Override
  public void visitJumpInsn(final int opcode, final Label destination) {
    if ((opcode == Opcodes.IFNE) && this.assertBlockStarted) {
      this.destination = destination;
      this.assertBlockStarted = false;
    }
    super.visitJumpInsn(opcode, destination);

  }

  @Override
  public void visitLabel(final Label label) {
    // delegate to child first to ensure visitLabel not in scope for mutation
    super.visitLabel(label);
    if (this.destination == label) {
      this.context.enableMutatations(DISABLE_REASON);
      this.destination = null;
    }
  }

}
