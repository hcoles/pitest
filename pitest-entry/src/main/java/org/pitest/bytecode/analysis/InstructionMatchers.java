package org.pitest.bytecode.analysis;

import static java.util.function.Predicate.isEqual;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
import static org.objectweb.asm.Opcodes.ICONST_M1;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.pitest.sequence.Result.result;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FrameNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.LineNumberNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.pitest.classinfo.ClassName;
import org.pitest.sequence.Match;
import org.pitest.sequence.Slot;
import org.pitest.sequence.SlotRead;
import org.pitest.sequence.SlotWrite;

import java.util.function.Predicate;

public class InstructionMatchers {

  public static Match<AbstractInsnNode> anyInstruction() {
    return Match.always();
  }

  /**
   * Matches nodes that do not represent an instruction or label
   */
  public static Match<AbstractInsnNode> notAnInstruction() {
     return isA(LineNumberNode.class).or(isA(FrameNode.class));
  }

  public static Match<AbstractInsnNode> newCall(ClassName target) {
    final String clazz = target.asInternalName();
    return (c, t) -> {
      if ( t instanceof TypeInsnNode ) {
        final TypeInsnNode call = (TypeInsnNode) t;
        return result(call.getOpcode() == Opcodes.NEW && call.desc.equals(clazz), c);
      }
      return result(false, c);
    };
  }

  public static Match<AbstractInsnNode> ldcString(Predicate<String> match) {
    return (c, t) -> {
      if ( t instanceof LdcInsnNode) {
        final LdcInsnNode ldc = (LdcInsnNode) t;
        return result(ldc.cst instanceof String && match.test((String) ldc.cst), c);
      }
      return result(false, c);
    };
  }

  public static Match<AbstractInsnNode> opCode(final int opcode) {
    return (c, a) -> result(a.getOpcode() == opcode, c);
  }

  public static <T extends AbstractInsnNode> Match<AbstractInsnNode> isA(
      final Class<T> cls) {
    return (c, a) -> result(a.getClass().isAssignableFrom(cls), c);
  }

  public static Match<AbstractInsnNode> incrementsVariable(final SlotRead<Integer> counterVariable) {
   return (context, a) -> result((a instanceof IincInsnNode)
       && context.retrieve(counterVariable).filter(isEqual(((IincInsnNode)a).var)).isPresent(), context);
  }

  public static Match<AbstractInsnNode> anIStore(
      final SlotWrite<Integer> counterVariable) {
    return opCode(Opcodes.ISTORE).and(aVariableAccess(counterVariable));
  }

  public static Match<AbstractInsnNode> anILoad(
          final SlotWrite<Integer> counterVariable) {
    return opCode(Opcodes.ILOAD).and(aVariableAccess(counterVariable));
  }

  public static Match<AbstractInsnNode> aVariableAccess(
      final SlotWrite<Integer> counterVariable) {
    return (c, t) -> {
      if (t instanceof VarInsnNode) {
        return result(true, c.store(counterVariable, ((VarInsnNode) t).var));
      }
      return result(false, c);
    };
  }

  public static Match<AbstractInsnNode> anIStoreTo(
      final SlotRead<Integer> counterVariable) {
    return opCode(ISTORE).and(variableMatches(counterVariable));
  }

  public static Match<AbstractInsnNode> anILoadOf(
      final SlotRead<Integer> counterVariable) {
    return opCode(ILOAD).and(variableMatches(counterVariable));
  }

  public static Match<AbstractInsnNode> variableMatches(
      final SlotRead<Integer> counterVariable) {
    return (c, t) -> result((t instanceof VarInsnNode)
        && c.retrieve(counterVariable).filter(isEqual(((VarInsnNode) t).var)).isPresent(), c);
  }


  public static Match<AbstractInsnNode> anIntegerConstant() {
    return opCode(ICONST_M1)
        .or(opCode(ICONST_0))
        .or(opCode(ICONST_1))
        .or(opCode(ICONST_2))
        .or(opCode(ICONST_3))
        .or(opCode(ICONST_4))
        .or(opCode(ICONST_5));
  }

  public static Match<AbstractInsnNode> aLabelNode(SlotWrite<LabelNode> slot) {
    return isA(LabelNode.class).and(writeNodeToSlot(slot, LabelNode.class));
  }

  public static Match<AbstractInsnNode> aJump() {
    return isA(JumpInsnNode.class);
  }

  public static Match<AbstractInsnNode> aConditionalJump() {
    return (c, t) -> result((t instanceof JumpInsnNode)
        && (t.getOpcode() != Opcodes.GOTO)
        && (t.getOpcode() != Opcodes.JSR), c);
  }

  public static Match<AbstractInsnNode> aConditionalJumpTo(Slot<LabelNode> label) {
    return jumpsTo(label.read()).and(aConditionalJump());
  }


  public static <T extends AbstractInsnNode> Match<AbstractInsnNode> writeNodeToSlot(final SlotWrite<T> slot, final Class<T> clazz) {
    return (c, t) -> {
      if (clazz.isAssignableFrom(t.getClass()) ) {
        return result(true, c.store(slot, clazz.cast(t)));
      }
      return result(false, c);
    };
  }

  public static  Match<AbstractInsnNode> methodCallThatReturns(final ClassName type) {
    return (c, t) -> {
      if ( t instanceof MethodInsnNode ) {
        return result(((MethodInsnNode) t).desc.endsWith(type.asInternalName() + ";"), c);
      }
      return result(false, c);
    };
  }

  public static  Match<AbstractInsnNode> methodCall() {
    return isA(MethodInsnNode.class);
  }
  
  public static Match<AbstractInsnNode> methodCallNamed(String name) {
    return (c, t) -> {
      if ( t instanceof MethodInsnNode ) {
        final MethodInsnNode call = (MethodInsnNode) t;
        return result(call.name.equals(name), c);
      }
      return result(false, c);
    };
  }

  public static  Match<AbstractInsnNode> methodDescEquals(final String desc) {
    return (c, t) -> {
      if ( t instanceof MethodInsnNode ) {
        return result(((MethodInsnNode) t).desc.equals(desc), c);
      }
      return result(false, c);
    };
  }

  public static  Match<AbstractInsnNode> methodCallTo(final ClassName owner, final String name) {
    return methodCallTo(owner, c -> c.equals(name));
  }

  public static  Match<AbstractInsnNode> methodCallTo(final ClassName owner, Predicate<String> name) {
    return (c, t) -> {
      if ( t instanceof MethodInsnNode ) {
        final MethodInsnNode call = (MethodInsnNode) t;
        return result( name.test(call.name) && call.owner.equals(owner.asInternalName()), c);
      }
      return result(false, c);
    };
  }

  public static  Match<AbstractInsnNode> isInstruction(final SlotRead<AbstractInsnNode> target) {
    return (c, t) -> result(c.retrieve(target).get() == t, c);
  }

  public static Match<AbstractInsnNode> getStatic(String owner, String field) {
    return (c, t) -> {
       if (t instanceof FieldInsnNode) {
         FieldInsnNode fieldNode = (FieldInsnNode) t;
         return result( t.getOpcode() == Opcodes.GETSTATIC && fieldNode.name.equals(field) && fieldNode.owner.equals(owner), c);
       }
      return result(false, c);
    };
  }

  /**
   * Records if a instruction matches the target, but always returns true
   */
  public static  Match<AbstractInsnNode> recordTarget(final SlotRead<AbstractInsnNode> target, final SlotWrite<Boolean> found) {
    return (c, t) -> {
      if (c.retrieve(target).get() == t) {
        return result(true, c.store(found, true));
      }
      return result(true, c);
    };
  }


  private static Match<AbstractInsnNode> storeJumpTarget(
      final SlotWrite<LabelNode> label) {
    return (c, t) -> {
      if (t instanceof JumpInsnNode ) {
        return result(true, c.store(label, ((JumpInsnNode) t).label));
      }
      return result(false, c);
    };
  }

  public static Match<AbstractInsnNode> jumpsTo(
      final SlotRead<LabelNode> loopStart) {
    return (context, a) -> {
      if (!(a instanceof JumpInsnNode)) {
        return result(false, context);
      }
      final JumpInsnNode jump = (JumpInsnNode) a;

      return result(context.retrieve(loopStart).filter(isEqual(jump.label)).isPresent(), context);
    };
  }

  public static Match<AbstractInsnNode> jumpsTo(
      final SlotWrite<LabelNode> label) {
    return storeJumpTarget(label);
  }

  public static Match<AbstractInsnNode> gotoLabel(
      final SlotWrite<LabelNode> loopEnd) {
        return opCode(Opcodes.GOTO).and(storeJumpTarget(loopEnd));
  }

  public static Match<AbstractInsnNode> labelNode(
      final SlotRead<LabelNode> loopEnd) {
    return (c, t) -> {
     if (!(t instanceof LabelNode)) {
       return result(false, c);
     }

     final LabelNode l = (LabelNode) t;
     return result(c.retrieve(loopEnd).filter(isEqual(l)).isPresent(), c);

    };
  }

  public static Match<AbstractInsnNode> debug(final String msg) {
    return (context, a) -> {
      context.debug(msg, a);
      return result(true, context);
    };
  }
}
