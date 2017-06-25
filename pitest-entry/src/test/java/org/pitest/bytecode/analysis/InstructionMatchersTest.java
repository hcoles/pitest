package org.pitest.bytecode.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import org.junit.Test;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.pitest.sequence.Context;

public class InstructionMatchersTest {

  private Context<AbstractInsnNode> unused;

  @Test
  public void anyInstructionShouldMatchAnything() {
    AbstractInsnNode node = new InsnNode(-1);
    assertTrue(anyInstruction().test(unused, node));
  }

  @Test
  public void opCodeShouldMatchOnOpcode() {
    AbstractInsnNode node = new InsnNode(-1);
    assertTrue(opCode(-1).test(unused, node));
    assertFalse(opCode(0).test(unused, node));
  }
  
  @Test
  public void isAShouldMatchOnType() {
    AbstractInsnNode node = new InsnNode(-1);
    assertTrue(isA(InsnNode.class).test(unused, node));
    assertFalse(isA(LabelNode.class).test(unused, node));
  }
}
