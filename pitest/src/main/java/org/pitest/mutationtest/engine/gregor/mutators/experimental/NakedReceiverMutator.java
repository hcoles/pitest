/*
 * Copyright 2015 Urs Metz
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

package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.POP2;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator for non-void methods whos return type matches
 * the receiver's type that replaces the method call with the receiver.
 * E. g. the method call
 * <pre>
 *   public int originalMethod() {
 *     String someString = "pit";
 *     return someString.toUpperCase();
 *   }
 * </pre>
 * is mutated to
 * <pre>
 *   public int mutatedMethod() {
 *     String someString = "pit";
 *     return someString;
 *   }
 * </pre>
 */
public enum NakedReceiverMutator implements MethodMutatorFactory {

  NAKED_RECEIVER;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ReplaceMethodCallWithObjectVisitor(context, methodVisitor, this);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return name();
  }

  static class ReplaceMethodCallWithObjectVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext      context;

    ReplaceMethodCallWithObjectVisitor(final MutationContext context,
        final MethodVisitor writer, final MethodMutatorFactory factory) {
      super(ASMVersion.ASM_VERSION, writer);
      this.factory = factory;
      this.context = context;
    }

    @Override
    public void visitMethodInsn(final int opcode, final String owner,
        final String name, final String desc, final boolean itf) {
      if (isNonStaticCall(opcode) && hasReturnTypeMatchingReceiverType(desc, owner)) {
        final MutationIdentifier newId = this.context
            .registerMutation(this.factory,
                "replaced call to " + owner + "::" + name + " with receiver");
        if (this.context.shouldMutate(newId)) {
          popMethodArgumentsFromStack(desc);
          return;
        }
        this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      } else {
        this.mv.visitMethodInsn(opcode, owner, name, desc, itf);
      }
    }

    private boolean hasReturnTypeMatchingReceiverType(final String desc,
        String owner) {
      return Type.getObjectType(owner).equals(Type.getReturnType(desc));
    }

    private void popMethodArgumentsFromStack(String desc) {
      final Type[] argumentTypes = Type.getArgumentTypes(desc);
      for (final Type argType : argumentTypes) {
        popArgument(argType);
      }
    }

    private void popArgument(final Type argumentType) {
      if (argumentType.getSize() != 1) {
        this.mv.visitInsn(POP2);
      } else {
        this.mv.visitInsn(POP);
      }
    }

    private boolean isNonStaticCall(int opcode) {
      return Opcodes.INVOKESTATIC != opcode;
    }
  }
}
