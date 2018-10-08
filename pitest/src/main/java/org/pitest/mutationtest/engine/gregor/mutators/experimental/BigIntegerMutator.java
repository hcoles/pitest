package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
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
    return "EXPERIMENTAL_BIGINTEGER_MUTATOR";
  }

  @Override
  public String getName() {
    return toString();
  }

  private static final class BigIntegerMathMutator extends MethodVisitor {

    private static final Map<String, Replacement> REPLACEMENTS;

    static {
      Map<String, Replacement> map = new HashMap<>();

      String unary = "(Ljava/math/BigInteger;)Ljava/math/BigInteger;";
      put(map, new Replacement("add", "subtract", unary));
      put(map, new Replacement("subtract", "add", unary));
      put(map, new Replacement("multiply", "divide", unary));
      put(map, new Replacement("divide", "multiply", unary));
      put(map, new Replacement("mod", "multiply", unary));
      put(map, new Replacement("remainder", "multiply", unary));

      put(map, new Replacement("and", "or", unary));
      put(map, new Replacement("or", "and", unary));
      put(map, new Replacement("xor", "and", unary));
      put(map, new Replacement("andNot", "and", unary));

      put(map, new Replacement("max", "min", unary));
      put(map, new Replacement("min", "max", unary));

      String unaryPrimitive = "(I)Ljava/math/BigInteger;";
      put(map, new Replacement("shiftLeft", "shiftRight", unaryPrimitive));
      put(map, new Replacement("shiftRight", "shiftLeft", unaryPrimitive));

      String intAsParam = "(I)Ljava/math/BigInteger;";
      put(map, new Replacement("setBit", "clearBit", intAsParam));
      put(map, new Replacement("clearBit", "setBit", intAsParam));
      put(map, new Replacement("flipBit", "setBit", intAsParam));

      String noParams = "()Ljava/math/BigInteger;";
      put(map, new Replacement("not", "negate", noParams));
      put(map, new Replacement("negate", "not", noParams));
      put(map, new Replacement("abs", "negate", noParams));

      REPLACEMENTS = Collections.unmodifiableMap(map);
    }

    private static void put(Map<String, Replacement> map, Replacement replacement) {
      map.put(replacement.sourceName, replacement);
    }

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private final String expectedOwner = "java/math/BigInteger";

    private BigIntegerMathMutator(MethodMutatorFactory factory, MutationContext context,
        MethodVisitor visitor) {
      super(Opcodes.ASM6, visitor);

      this.factory = factory;
      this.context = context;
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor,
        boolean isInterface) {
      if (!owner.equals(expectedOwner) || opcode != Opcodes.INVOKEVIRTUAL) {
        this.mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
        return;
      }

      Replacement replacement = REPLACEMENTS.get(name);
      if (replacement != null && replacement.descriptor.equals(descriptor)) {
        MutationIdentifier identifier = context.registerMutation(factory, replacement.toString());
        if (context.shouldMutate(identifier)) {
          this.mv.visitMethodInsn(
              opcode,
              owner,
              replacement.destinationName,
              replacement.descriptor,
              false);
          return;
        }
      }

      this.mv.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
        Object... bootstrapMethodArguments) {
      bootstrapMethodHandle = mutateHandle(bootstrapMethodHandle);
      Object[] methodArgs = new Object[bootstrapMethodArguments.length];
      for (int i = 0; i < bootstrapMethodArguments.length; i++) {
        Object bootstrapMethodArgument = bootstrapMethodArguments[i];
        if (bootstrapMethodArgument instanceof Handle) {
          methodArgs[i] = mutateHandle((Handle) bootstrapMethodArgument);
        } else {
          methodArgs[i] = bootstrapMethodArgument;
        }
      }
      super.visitInvokeDynamicInsn(name, descriptor, bootstrapMethodHandle, methodArgs);
    }

    /**
     * Mutates a handle within an invoke virtual.
     */
    private Handle mutateHandle(Handle handle) {
      int opcode = handle.getTag();
      String owner = handle.getOwner();
      String name = handle.getName();
      String descriptor = handle.getDesc();

      if (owner.equals(expectedOwner) && opcode == Opcodes.H_INVOKEVIRTUAL) {
        if (REPLACEMENTS.containsKey(name)) {
          Replacement replacement = REPLACEMENTS.get(name);
          if (replacement.descriptor.equals(descriptor)) {
            MutationIdentifier id = context.registerMutation(factory, replacement.toString());
            if (context.shouldMutate(id)) {
              return new Handle(
                  opcode,
                  owner,
                  replacement.destinationName,
                  descriptor,
                  handle.isInterface());
            }
          }
        }
      }
      return handle;
    }

    private static final class Replacement {

      private final String sourceName;
      private final String destinationName;
      private final String descriptor;

      Replacement(String sourceName, String destinationName, String descriptor) {
        this.sourceName = sourceName;
        this.destinationName = destinationName;
        this.descriptor = descriptor;
      }

      @Override
      public String toString() {
        String template = "Replaced BigInteger#%s with BigInteger#%s.";
        return String.format(template, sourceName, destinationName);
      }
    }
  }
}
