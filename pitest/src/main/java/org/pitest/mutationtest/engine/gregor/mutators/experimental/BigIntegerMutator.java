package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum BigIntegerMutator implements MethodMutatorFactory {
  INSTANCE;

  @Override
  public MethodVisitor create(MutationContext context, MethodInfo info, MethodVisitor visitor) {
    return new BigIntegerMathMutator(this, context, visitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String toString() {
    return "EXPERIMENTAL_BIGINTEGER_MATH_MUTATOR";
  }

  @Override
  public String getName() {
    return toString();
  }

  private static final class BigIntegerMathMutator extends MethodVisitor {

    private static final Map<String, String> REPLACEMENTS;

    static {
      Map<String, String> replacements = new HashMap<>();
      replacements.put("add", "subtract");
      replacements.put("subtract", "add");
      replacements.put("multiply", "divide");
      replacements.put("divide", "multiply");
      replacements.put("mod", "divide");

      replacements.put("shiftLeft", "shiftRight");
      replacements.put("shiftRight", "shiftLeft");
      replacements.put("and", "or");
      replacements.put("or", "and");
      replacements.put("xor", "or");

      replacements.put("max", "min");
      replacements.put("min", "max");

      // TODO not, negate, andNot, setBit, clearBit
      REPLACEMENTS = Collections.unmodifiableMap(replacements);
    }


    private final MethodMutatorFactory factory;
    private final MutationContext context;

    private String descriptor;

    private BigIntegerMathMutator(MethodMutatorFactory factory, MutationContext context,
        MethodVisitor visitor) {
      super(Opcodes.ASM6, visitor);

      this.factory = factory;
      this.context = context;

      init();
    }

    private void init() {
      try {
        Class<BigInteger> type = BigInteger.class;
        descriptor = Type.getMethodDescriptor(type.getMethod("add", type));
      } catch (NoSuchMethodException e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
        Object... bootstrapMethodArguments) {
      super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle,
          bootstrapMethodArguments);
      System.out.println(
          "name = [" + name + "], descriptor = [" + descriptor + "], bootstrapMethodHandle = ["
              + bootstrapMethodHandle + "], bootstrapMethodArguments = [" + bootstrapMethodArguments
              + "]");
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
        boolean isInterface) {
      if (!owner.equals("java/math/BigInteger")) {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        return;
      }

      String replacement = REPLACEMENTS.get(name);
      if (replacement != null) {
        context.registerMutation(factory, "Hello WOrlllld!");
        this.mv.visitMethodInsn(opcode, owner, replacement,
            "(Ljava/math/BigInteger;)Ljava/math/BigInteger;");
      } else {
        super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
      }
    }
  }
}
