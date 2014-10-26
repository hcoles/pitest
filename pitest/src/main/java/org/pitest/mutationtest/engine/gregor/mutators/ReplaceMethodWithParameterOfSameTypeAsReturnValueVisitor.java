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

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

import static java.util.Arrays.asList;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;

class ReplaceMethodWithParameterOfSameTypeAsReturnValueVisitor
    extends MethodVisitor {

  private final MethodMutatorFactory factory;
  private final MutationContext      context;

  public ReplaceMethodWithParameterOfSameTypeAsReturnValueVisitor(
      final MutationContext context, final MethodVisitor writer,
      final MethodMutatorFactory factory) {
    super(Opcodes.ASM5, writer);
    this.factory = factory;
    this.context = context;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {
    if (hasParameterMatchingTheReturnType(desc)) {
      final MutationIdentifier newId = this.context.registerMutation(
          this.factory,
          "replaced call to " + owner + "::" + name + " with parameter");
      if (context.shouldMutate(newId)) {
        replaceMethodCallWithParameterMatchingTheReturnType(
            Type.getArgumentTypes(desc), Type.getReturnType(desc)
        );
      } else {
        this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      }
    } else {
      this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
    }
  }

  private boolean hasParameterMatchingTheReturnType(String desc) {
    final Type returnType = Type.getReturnType(desc);
    final Type[] argTypes = Type.getArgumentTypes(desc);
    return asList(argTypes).contains(returnType);
  }

  private void replaceMethodCallWithParameterMatchingTheReturnType(
      Type[] argTypes, Type returnType) {
    for (int i = argTypes.length - 1; i >= 0; i--) {
      final Type argumentType = argTypes[i];
      if (argumentType.equals(returnType)) {
        return;
      } else {
        popParameter(argumentType);
      }
    }
  }

  private void popParameter(Type argumentType) {
    if (argumentType.getSize() != 1) {
      this.mv.visitInsn(POP2);
    } else {
      this.mv.visitInsn(POP);
    }
  }

}
