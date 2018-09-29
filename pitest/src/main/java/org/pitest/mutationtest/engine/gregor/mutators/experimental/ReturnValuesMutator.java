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

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * The <code>ReturnValuesMutator</code> mutates the return values of method
 * calls. Depending on the return type of the method another mutation is used.
 *
 * <p>
 * Replacements for primitive types are simple. Replacements of object
 * references are handled by ObjectReferenceReplacer. Those replacements can get
 * more complex.
 * </p>
 *
 *
 * @author Stefan Penndorf &lt;stefan.penndorf@gmail.com&gt;
 */
public class ReturnValuesMutator implements MethodMutatorFactory {

  private static final class ObjectMutationMethod {

    private final String mutatorMethodName;
    private final String mutatorInternalName;
    private final String mutationMethodDescriptor;

    ObjectMutationMethod() {

      final Type mutatorType = Type.getType(ReturnValuesMutator.class);
      this.mutatorInternalName = mutatorType.getInternalName();

      this.mutatorMethodName = "mutateObjectInstance";

      final Type objectType = Type.getType(Object.class);
      final Type classType = Type.getType(Class.class);
      this.mutationMethodDescriptor = Type.getMethodDescriptor(objectType,
          new Type[] { objectType, classType });
    }

    public String getClassName() {
      return this.mutatorInternalName;
    }

    public String getMethodDescriptor() {
      return this.mutationMethodDescriptor;
    }

    public String getMethodName() {
      return this.mutatorMethodName;
    }

  }

  private static final class ObjectReferenceReplacer {

    /**
     * See {@link ReturnValuesMutator#mutateObjectInstance(Object, Class)} for
     * details.
     */
    private Object replaceObjectInstance(final Object object,
        final Class<?> declaredReturnType) {

      if (Boolean.class == declaredReturnType) {
        if (Boolean.TRUE.equals(object)) {
          return Boolean.FALSE;
        } else {
          return Boolean.TRUE;
        }
      }

      if (Integer.class == declaredReturnType) {
        final Integer intValue = (Integer) object;
        if (intValue == null) {
          return Integer.valueOf(1);
        } else if (intValue == 1) {
          return Integer.valueOf(0);
        } else {
          return intValue + 1;
        }
      }

      if (Long.class == declaredReturnType) {
        final Long longValue = (Long) object;
        if (longValue == null) {
          return Long.valueOf(1L);
        } else {
          return longValue + 1L;
        }
      }

      if (Object.class == declaredReturnType) {
        if (object != null) {
          return null;
        } else {
          return new Object();
        }
      }

      if (object == null) {
        throw new RuntimeException(
            "Mutated return of null object to throwing a runtime exception");
      }

      return null;
    }

  }

  private final class ReturnValuesMethodVisitor extends MethodVisitor {

    private static final String   DESCRIPTION_MESSAGE_PATTERN = "replaced return of %s value with %s";

    private final MutationContext context;
    private final MethodInfo      methodInfo;

    private ReturnValuesMethodVisitor(final MutationContext context,
        final MethodInfo methodInfo, final MethodVisitor delegateVisitor) {
      super(ASMVersion.ASM_VERSION, delegateVisitor);
      this.context = context;
      this.methodInfo = methodInfo;
    }

    private void mutateObjectReferenceReturn() {
      if (shouldMutate("object reference", "[see docs for details]")) {
        final Type returnType = this.methodInfo.getReturnType();

        super.visitLdcInsn(returnType);
        super.visitMethodInsn(Opcodes.INVOKESTATIC,
            OBJECT_MUTATION_METHOD.getClassName(),
            OBJECT_MUTATION_METHOD.getMethodName(),
            OBJECT_MUTATION_METHOD.getMethodDescriptor(), false);
        super.visitTypeInsn(Opcodes.CHECKCAST, returnType.getInternalName());
      }
      super.visitInsn(Opcodes.ARETURN);
    }

    /**
     * Mutates a primitive double return (<code>Opcode.DRETURN</code>). The
     * strategy used was translated from jumble BCEL code. The following is
     * complicated by the problem of <tt>NaN</tt>s. By default the new value is
     * <code>-(x + 1)</code>, but this doesn't work for <tt>NaN</tt>s. But for a
     * <tt>NaN</tt> <code>x != x</code> is true, and we use this to detect them.
     *
     * @see #mutatePrimitiveFloatReturn()
     */
    private void mutatePrimitiveDoubleReturn() {
      if (shouldMutate("primitive double", "(x != NaN)? -(x + 1) : -1 ")) {
        final Label label = new Label();

        super.visitInsn(Opcodes.DUP2);
        super.visitInsn(Opcodes.DUP2);
        super.visitInsn(Opcodes.DCMPG);
        super.visitJumpInsn(Opcodes.IFEQ, label);

        super.visitInsn(Opcodes.POP2);
        super.visitInsn(Opcodes.DCONST_0);

        // the following code is executed in NaN case, too
        super.visitLabel(label);
        super.visitInsn(Opcodes.DCONST_1);
        super.visitInsn(Opcodes.DADD);
        super.visitInsn(Opcodes.DNEG);
        super.visitInsn(Opcodes.DRETURN);
      }
    }

    /**
     * Mutates a primitive float return (<code>Opcode.FRETURN</code>). The
     * strategy used was translated from jumble BCEL code. The following is
     * complicated by the problem of <tt>NaN</tt>s. By default the new value is
     * <code>-(x + 1)</code>, but this doesn't work for <tt>NaN</tt>s. But for a
     * <tt>NaN</tt> <code>x != x</code> is true, and we use this to detect them.
     *
     * @see #mutatePrimitiveDoubleReturn()
     */
    private void mutatePrimitiveFloatReturn() {
      if (shouldMutate("primitive float", "(x != NaN)? -(x + 1) : -1 ")) {
        final Label label = new Label();

        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.FCMPG);
        super.visitJumpInsn(Opcodes.IFEQ, label);

        super.visitInsn(Opcodes.POP);
        super.visitInsn(Opcodes.FCONST_0);

        // the following code is executed in NaN case, too
        super.visitLabel(label);
        super.visitInsn(Opcodes.FCONST_1);
        super.visitInsn(Opcodes.FADD);
        super.visitInsn(Opcodes.FNEG);
        super.visitInsn(Opcodes.FRETURN);
      }
    }

    private void mutatePrimitiveIntegerReturn() {

      if (shouldMutate("primitive boolean/byte/short/integer",
          "(x == 1) ? 0 : x + 1")) {
        final Label label = new Label();

        super.visitInsn(Opcodes.DUP);
        super.visitInsn(Opcodes.ICONST_1);
        super.visitJumpInsn(Opcodes.IF_ICMPEQ, label);

        super.visitInsn(Opcodes.ICONST_1);
        super.visitInsn(Opcodes.IADD);
        super.visitInsn(Opcodes.IRETURN);

        super.visitLabel(label);
        super.visitInsn(Opcodes.ICONST_0);
        super.visitInsn(Opcodes.IRETURN);
      }
    }

    private void mutatePrimitiveLongReturn() {
      if (shouldMutate("primitive long", "x + 1")) {
        super.visitInsn(Opcodes.LCONST_1);
        super.visitInsn(Opcodes.LADD);
        super.visitInsn(Opcodes.LRETURN);
      }
    }

    private boolean shouldMutate(final String type, final String replacement) {

      final String description = String.format(DESCRIPTION_MESSAGE_PATTERN,
          type, replacement);

      final MutationIdentifier mutationId = this.context.registerMutation(
          ReturnValuesMutator.this, description);

      return this.context.shouldMutate(mutationId);

    }

    @Override
    public void visitInsn(final int opcode) {

      switch (opcode) {
      case Opcodes.IRETURN:
        mutatePrimitiveIntegerReturn();
        break;
      case Opcodes.LRETURN:
        mutatePrimitiveLongReturn();
        break;
      case Opcodes.FRETURN:
        mutatePrimitiveFloatReturn();
        break;
      case Opcodes.DRETURN:
        mutatePrimitiveDoubleReturn();
        break;
      case Opcodes.ARETURN:
        mutateObjectReferenceReturn();
        break;
      default:
        super.visitInsn(opcode);
        break;
      }
    }

  }

  /**
   * Do not change thread safe singleton instantiation.
   */
  private static final ObjectMutationMethod    OBJECT_MUTATION_METHOD = new ObjectMutationMethod();

  /**
   * Do not change thread safe singleton instantiation.
   */
  private static final ObjectReferenceReplacer SINGLETON_REPLACER     = new ObjectReferenceReplacer();

  /**
   * Mutates a given object instance / reference. The reference
   * <code>object</code> may be <code>null</code>. The class given as second
   * parameter <code>clazz</code> will be the class of the <code>object</code>
   * or one of it's super classes. The returned object must be of type
   * <code>clazz</code> or one of it's child classes.
   *
   * @param object
   *          the object reference to mutate, maybe <code>null</code>.
   * @param clazz
   *          the type the returned object must have. Usually that's also the
   *          type of <code>object</code> but might be a super type of
   *          <code>object</code>.
   * @return the mutated object reference (can also be <code>null</code>).
   */
  public static Object mutateObjectInstance(final Object object,
      final Class<?> clazz) {
    return SINGLETON_REPLACER.replaceObjectInstance(object, clazz);
  }

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ReturnValuesMethodVisitor(context, methodInfo, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return "EXPERIMENTAL_RETURN_VALUES_MUTATOR";
  }

}
