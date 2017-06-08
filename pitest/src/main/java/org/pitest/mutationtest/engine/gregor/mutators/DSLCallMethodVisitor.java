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
package org.pitest.mutationtest.engine.gregor.mutators;

import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.functional.F2;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

class DSLCallMethodVisitor extends MethodVisitor {

  private final F2<String, String, Boolean> filter;
  private final MethodMutatorFactory        factory;
  private final MutationContext                     context;
  private final MethodInfo                  methodInfo;

  public DSLCallMethodVisitor(final MethodInfo methodInfo,
      final MutationContext context, final MethodVisitor writer,
      final MethodMutatorFactory factory,
      final F2<String, String, Boolean> filter) {
    super(Opcodes.ASM5, writer);
    this.factory = factory;
    this.filter = filter;
    this.context = context;
    this.methodInfo = methodInfo;
  }

  @Override
  public void visitMethodInsn(final int opcode, final String owner,
      final String name, final String desc, boolean itf) {

    if (!filter(opcode, owner, name, desc, itf)) {
      this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
    } else {
      final MutationIdentifier newId = this.context.registerMutation(
          this.factory, "removed call to " + owner + "::" + name);

      if (this.context.shouldMutate(newId)) {

        popStack(desc, name);

      } else {
        this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      }
    }

  }

  protected boolean filter(final int opcode, final String owner,
          final String name, final String desc, boolean itf) {
    return this.filter.apply(name, desc)
          && !isCallToSuperOrOwnConstructor(name, owner)
          && MethodInfo.doesReturnOwner(owner,desc);
  }

  private boolean isCallToSuperOrOwnConstructor(final String name,
      final String owner) {
    return this.methodInfo.isConstructor()
        && MethodInfo.isConstructor(name)
        && (owner.equals(this.context.getClassInfo().getName()) || this.context
            .getClassInfo().getSuperName().equals(owner));
  }

  private void popStack(final String desc, final String name) {
    final Type[] argTypes = Type.getArgumentTypes(desc);
    for (int i = argTypes.length - 1; i >= 0; i--) {
      final Type argumentType = argTypes[i];
      if (argumentType.getSize() != 1) {
        this.mv.visitInsn(POP2);
      } else {
        this.mv.visitInsn(POP);
      }
    }

    if (MethodInfo.isConstructor(name)) {
      this.mv.visitInsn(POP);
    }
  }

}
