package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collections;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.pitest.functional.Option;
import org.pitest.sequence.Context;
import org.pitest.sequence.Slot;

public class InstructionMatchersTest {

  private Context<AbstractInsnNode> context = Context.start(Collections.<AbstractInsnNode>emptyList());

  @Test
  public void anyInstructionShouldMatchAnything() {
    AbstractInsnNode node = new InsnNode(-1);
    assertTrue(anyInstruction().test(context, node));
  }

  @Test
  public void opCodeShouldMatchOnOpcode() {
    AbstractInsnNode node = new InsnNode(-1);
    assertTrue(opCode(-1).test(context, node));
    assertFalse(opCode(0).test(context, node));
  }
  
  @Test
  public void isAShouldMatchOnType() {
    AbstractInsnNode node = new InsnNode(-1);
    assertTrue(isA(InsnNode.class).test(context, node));
    assertFalse(isA(LabelNode.class).test(context, node));
  }
  
  @Test
  public void shouldMatchIncrementsToStoredLocalVariable() {
    Slot<Integer> slot = Slot.create(Integer.class);
    context.store(slot.write(), 42);
    IincInsnNode node = new IincInsnNode(42, 1);
    assertTrue(incrementsVariable(slot.read()).test(context,node));
  }
  
  @Test
  public void shouldNotMatchIncrementsToDifferentLocalVariable() {
    Slot<Integer> slot = Slot.create(Integer.class);
    context.store(slot.write(), 42);
    IincInsnNode node = new IincInsnNode(42 + 1, 1);
    assertFalse(incrementsVariable(slot.read()).test(context,node));
  }
  
  @Test
  public void shouldCaptureIStoreVariable() {
    Slot<Integer> slot = Slot.create(Integer.class);
    VarInsnNode node = new VarInsnNode(Opcodes.ISTORE, 3);
    assertTrue(anIStore(slot.write()).test(context,node));
    assertThat(context.retrieve(slot.read())).isEqualTo(Option.some(3));
  }
}
