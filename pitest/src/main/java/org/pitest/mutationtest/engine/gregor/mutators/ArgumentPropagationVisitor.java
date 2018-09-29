/*
 * Copyright 2014 Stefan Mandel, Urs Metz
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
package org.pitest.mutationtest.engine.gregor.mutators;

import static java.util.Arrays.asList;
import static org.objectweb.asm.Opcodes.DUP2_X1;
import static org.objectweb.asm.Opcodes.DUP2_X2;
import static org.objectweb.asm.Opcodes.DUP_X2;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;
import static org.objectweb.asm.Opcodes.SWAP;

import java.util.Arrays;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

class ArgumentPropagationVisitor extends MethodVisitor {

  private final MethodMutatorFactory factory;
  private final MutationContext      context;

  ArgumentPropagationVisitor(final MutationContext context,
      final MethodVisitor writer, final MethodMutatorFactory factory) {
    super(ASMVersion.ASM_VERSION, writer);
    this.factory = factory;
    this.context = context;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, final boolean itf) {
    if (hasArgumentMatchingTheReturnType(desc)) {
      final MutationIdentifier newId = this.context.registerMutation(
          this.factory, "replaced call to " + owner + "::" + name
          + " with argument");
      if (this.context.shouldMutate(newId)) {
        final Type returnType = Type.getReturnType(desc);
        replaceMethodCallWithArgumentHavingSameTypeAsReturnValue(
            Type.getArgumentTypes(desc), returnType, opcode);
      } else {
        this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      }
    } else {
      this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
    }
  }

  private boolean hasArgumentMatchingTheReturnType(final String desc) {
    return findLastIndexOfArgumentWithSameTypeAsReturnValue(
        Type.getArgumentTypes(desc), Type.getReturnType(desc)) > -1;
  }

  private void replaceMethodCallWithArgumentHavingSameTypeAsReturnValue(
      final Type[] argTypes, final Type returnType, final int opcode) {
    final int indexOfPropagatedArgument = findLastIndexOfArgumentWithSameTypeAsReturnValue(
        argTypes, returnType);
    popArgumentsBeforePropagatedArgument(argTypes, indexOfPropagatedArgument);
    popArgumentsFollowingThePropagated(argTypes, returnType,
        indexOfPropagatedArgument);
    removeThisFromStackIfNotStatic(returnType, opcode);
  }

  private int findLastIndexOfArgumentWithSameTypeAsReturnValue(
      final Type[] argTypes, final Type returnType) {
    return asList(argTypes).lastIndexOf(returnType);
  }

  private void popArgumentsBeforePropagatedArgument(final Type[] argTypes,
      final int indexOfPropagatedArgument) {
    final Type[] argumentTypesBeforeNewReturnValue = Arrays.copyOfRange(
        argTypes, indexOfPropagatedArgument + 1, argTypes.length);
    popArguments(argumentTypesBeforeNewReturnValue);
  }

  private void popArguments(final Type[] argumentTypes) {
    for (int i = argumentTypes.length - 1; i >= 0; i--) {
      popArgument(argumentTypes[i]);
    }
  }

  private void popArgumentsFollowingThePropagated(final Type[] argTypes,
      final Type returnType, final int indexOfPropagatedArgument) {
    final Type[] argsFollowing = Arrays.copyOfRange(argTypes, 0,
        indexOfPropagatedArgument);
    for (int j = argsFollowing.length - 1; j >= 0; j--) {
      swap(this.mv, returnType, argsFollowing[j]);
      popArgument(argsFollowing[j]);
    }
  }

  private void removeThisFromStackIfNotStatic(final Type returnType,
      final int opcode) {
    if (isNotStatic(opcode)) {
      swap(this.mv, returnType, Type.getType(Object.class));
      this.mv.visitInsn(POP);
    }
  }

  private void popArgument(final Type argumentType) {
    if (argumentType.getSize() != 1) {
      this.mv.visitInsn(POP2);
    } else {
      this.mv.visitInsn(POP);
    }
  }

  private static boolean isNotStatic(final int opcode) {
    return INVOKESTATIC != opcode;
  }

  // based on: http://stackoverflow.com/a/11359551
  private static void swap(final MethodVisitor mv, final Type stackTop,
      final Type belowTop) {
    if (stackTop.getSize() == 1) {
      if (belowTop.getSize() == 1) {
        // Top = 1, below = 1
        mv.visitInsn(SWAP);
      } else {
        // Top = 1, below = 2
        mv.visitInsn(DUP_X2);
        mv.visitInsn(POP);
      }
    } else {
      if (belowTop.getSize() == 1) {
        // Top = 2, below = 1
        mv.visitInsn(DUP2_X1);
      } else {
        // Top = 2, below = 2
        mv.visitInsn(DUP2_X2);
      }
      mv.visitInsn(POP2);
    }
  }

}
