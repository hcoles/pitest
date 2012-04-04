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
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Disables mutations within java assertions
 * 
 */
public class AvoidAssertsMethodAdapter extends MethodAdapter {

  private final static String DISABLE_REASON = "ASSERTS";

  private final Context       context;
  private boolean             assertBlockStarted;
  private Label               destination;

  public AvoidAssertsMethodAdapter(final Context context,
      final MethodVisitor delegateMethodVisitor) {
    super(delegateMethodVisitor);
    this.context = context;
  }

  // disable beteween
  // mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Class",
  // "desiredAssertionStatus", "()Z");
  // and
  // mv.visitFieldInsn(PUTSTATIC,
  // "org/pitest/mutationtest/engine/gregor/TestGregorMutater$HasAssertStatement",
  // "$assertionsDisabled", "Z");
  // in <clinit> methods

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc) {

    if (opcode == Opcodes.INVOKEVIRTUAL && owner.equals("java/lang/Class")
        && name.equals("desiredAssertionStatus")) {
      this.context.disableMutations(DISABLE_REASON);
    }
    super.visitMethodInsn(opcode, owner, name, desc);
  }

  @Override
  public void visitFieldInsn(final int opcode, final String owner,
      final String name, final String desc) {

    if (name.equals("$assertionsDisabled")) {
      if (opcode == Opcodes.GETSTATIC) {
        context.disableMutations(DISABLE_REASON);
        assertBlockStarted = true;
      } else if (opcode == Opcodes.PUTSTATIC) {
        context.enableMutatations(DISABLE_REASON);
      }
    }
    super.visitFieldInsn(opcode, owner, name, desc);

  }

  @Override
  public void visitJumpInsn(int opcode, Label destination) {
    if ((opcode == Opcodes.IFNE) && assertBlockStarted) {
      this.destination = destination;
      assertBlockStarted = false;
    }
    super.visitJumpInsn(opcode, destination);

  }

  @Override
  public void visitLabel(Label label) {
    // delegate to child first to ensure visitLabel not in scope for mutation
    super.visitLabel(label);
    if (this.destination == label) {
      context.enableMutatations(DISABLE_REASON);
      destination = null;
    }
  }

}
