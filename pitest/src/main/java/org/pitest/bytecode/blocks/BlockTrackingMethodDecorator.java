/*
 * Copyright 2011 Henry Coles
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
package org.pitest.bytecode.blocks;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;

public class BlockTrackingMethodDecorator extends MethodAdapter {

  private final BlockCounter blockCounter;
  private final Set<Label> handlers = new HashSet<Label>();

  public BlockTrackingMethodDecorator(final BlockCounter blockCounter, final MethodVisitor mv) {
    super(mv);
    this.blockCounter = blockCounter;
  }

  @Override
  public void visitInsn(final int opcode) {
    this.mv.visitInsn(opcode);
    if (endsBlock(opcode)) {
      this.blockCounter.registerFinallyBlockEnd();
      this.blockCounter.registerNewBlock();
    }
  }

  @Override
  public void visitJumpInsn(final int arg0, final Label arg1) {
    this.mv.visitJumpInsn(arg0, arg1);
    this.blockCounter.registerNewBlock();
  }
  
  @Override
  public void visitTryCatchBlock(Label start, Label end, Label handler, String type) {
    super.visitTryCatchBlock(start, end, handler, type);
    if ( type == null ) {
      handlers.add(handler);
    }
  }
  
  @Override
  public void visitLabel(Label label) {
    super.visitLabel(label);
    if ( handlers.contains(label) ) {
      blockCounter.registerFinallyBlockStart();
    }
  }
  
  private boolean endsBlock(final int opcode) {
    switch (opcode) {
    case RETURN:
    case ARETURN:
    case DRETURN:
    case FRETURN:
    case IRETURN:
    case LRETURN:
    case ATHROW: // dubious if this is needed
      return true;
    default:
      return false;
    }
  }

}