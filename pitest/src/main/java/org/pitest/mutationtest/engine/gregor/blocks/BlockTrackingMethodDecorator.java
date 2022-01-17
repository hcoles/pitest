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
package org.pitest.mutationtest.engine.gregor.blocks;

import org.objectweb.asm.Handle;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.tree.MethodNode;
import org.pitest.bytecode.ASMVersion;
import org.pitest.coverage.analysis.Block;
import org.pitest.coverage.analysis.ControlFlowAnalyser;

import java.util.ArrayDeque;
import java.util.Deque;

public class BlockTrackingMethodDecorator extends MethodNode {

  private final BlockCounter  blockCounter;
  private final MethodVisitor cmv;

  public BlockTrackingMethodDecorator(final BlockCounter blockCounter,
      final MethodVisitor mv, int acc, String name, String desc,
      String signature, String[] exceptions) {
    super(ASMVersion.ASM_VERSION, acc, name, desc, signature, exceptions);
    this.cmv = mv;
    this.blockCounter = blockCounter;
  }

  @Override
  public void visitEnd() {
    super.visitEnd();

    final Deque<Block> blocks = new ArrayDeque<>(
        ControlFlowAnalyser.analyze(this));

    blockCounter.registerNewMethodStart();
    this.accept(new MethodVisitor(ASMVersion.ASM_VERSION, cmv) {
      Block curBlock = blocks.pop();
      int i;

      private void visitAnything() {
        if ((i == curBlock.getFirstInstruction())) {
          if (i == 0) {
            //ignore the very first block since we are 0 based
            if (!blocks.isEmpty()) {
              curBlock = blocks.pop();
            }
          } else {
            blockCounter.registerNewBlock();
            if (!blocks.isEmpty()) {
              curBlock = blocks.pop();
            }
          }
        }
        i++;
      }

      @Override
      public void visitFrame(int type, int numLocal, Object[] local,
          int numStack, Object[] stack) {
        visitAnything();
        super.visitFrame(type, numLocal, local, numStack, stack);
      }

      @Override
      public void visitInsn(int opcode) {
        visitAnything();
        super.visitInsn(opcode);
      }

      @Override
      public void visitIntInsn(int opcode, int operand) {
        visitAnything();
        super.visitIntInsn(opcode, operand);
      }

      @Override
      public void visitVarInsn(int opcode, int var) {
        visitAnything();
        super.visitVarInsn(opcode, var);
      }

      @Override
      public void visitTypeInsn(int opcode, String type) {
        visitAnything();
        super.visitTypeInsn(opcode, type);
      }

      @Override
      public void visitFieldInsn(int opcode, String owner, String name,
          String descriptor) {
        visitAnything();
        super.visitFieldInsn(opcode, owner, name, descriptor);
      }

      @Override
      public void visitMethodInsn(int opcode, String owner, String name,
          String descriptor, boolean isInterface) {
        visitAnything();
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      }

      @Override
      public void visitInvokeDynamicInsn(String name, String descriptor,
          Handle bootstrapMethodHandle, Object... bootstrapMethodArguments) {
        visitAnything();
        super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle,
            bootstrapMethodArguments);
      }

      @Override
      public void visitJumpInsn(int opcode, Label label) {
        visitAnything();
        super.visitJumpInsn(opcode, label);
      }

      @Override
      public void visitLabel(Label label) {
        visitAnything();
        super.visitLabel(label);
      }

      @Override
      public void visitLdcInsn(Object value) {
        visitAnything();
        super.visitLdcInsn(value);
      }

      @Override
      public void visitIincInsn(int var, int increment) {
        visitAnything();
        super.visitIincInsn(var, increment);
      }

      @Override
      public void visitTableSwitchInsn(int min, int max, Label dflt,
          Label... labels) {
        visitAnything();
        super.visitTableSwitchInsn(min, max, dflt, labels);
      }

      @Override
      public void visitLookupSwitchInsn(Label dflt, int[] keys,
          Label[] labels) {
        visitAnything();
        super.visitLookupSwitchInsn(dflt, keys, labels);
      }

      @Override
      public void visitMultiANewArrayInsn(String descriptor,
          int numDimensions) {
        visitAnything();
        super.visitMultiANewArrayInsn(descriptor, numDimensions);
      }

      @Override
      public void visitLineNumber(int line, Label start) {
        visitAnything();
        super.visitLineNumber(line, start);
      }
    });
  }
}