package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.bytecode.analysis.InstructionMatchers.aConditionalJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aJump;
import static org.pitest.bytecode.analysis.InstructionMatchers.aLabelNode;
import static org.pitest.bytecode.analysis.InstructionMatchers.anILoadOf;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStore;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIStoreTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.anIntegerConstant;
import static org.pitest.bytecode.analysis.InstructionMatchers.anyInstruction;
import static org.pitest.bytecode.analysis.InstructionMatchers.incrementsVariable;
import static org.pitest.bytecode.analysis.InstructionMatchers.isA;
import static org.pitest.bytecode.analysis.InstructionMatchers.methodCallTo;
import static org.pitest.bytecode.analysis.InstructionMatchers.opCode;

import java.util.Collections;

import org.junit.Test;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.VarInsnNode;
import org.pitest.classinfo.ClassName;
import java.util.Optional;
import org.pitest.sequence.Context;
import org.pitest.sequence.Slot;

public class InstructionMatchersTest {

  private final Context<AbstractInsnNode> context = Context.start(Collections.<AbstractInsnNode>emptyList());

  @Test
  public void anyInstructionShouldMatchAnything() {
    final AbstractInsnNode node = new InsnNode(-1);
    assertTrue(anyInstruction().test(this.context, node));
  }

  @Test
  public void opCodeShouldMatchOnOpcode() {
    final AbstractInsnNode node = new InsnNode(-1);
    assertTrue(opCode(-1).test(this.context, node));
    assertFalse(opCode(0).test(this.context, node));
  }

  @Test
  public void isAShouldMatchOnType() {
    final AbstractInsnNode node = new InsnNode(-1);
    assertTrue(isA(InsnNode.class).test(this.context, node));
    assertFalse(isA(LabelNode.class).test(this.context, node));
  }

  @Test
  public void shouldMatchIncrementsToStoredLocalVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    this.context.store(slot.write(), 42);
    final IincInsnNode node = new IincInsnNode(42, 1);
    assertTrue(incrementsVariable(slot.read()).test(this.context,node));
  }

  @Test
  public void shouldNotMatchIncrementsToDifferentLocalVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    this.context.store(slot.write(), 42);
    final IincInsnNode node = new IincInsnNode(42 + 1, 1);
    assertFalse(incrementsVariable(slot.read()).test(this.context,node));
  }

  @Test
  public void shouldCaptureIStoreVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    final VarInsnNode node = new VarInsnNode(Opcodes.ISTORE, 3);
    assertTrue(anIStore(slot.write()).test(this.context,node));
    assertThat(this.context.retrieve(slot.read())).isEqualTo(Optional.ofNullable(3));
  }

  @Test
  public void shouldMatchAgainstCapturedIStoreVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    this.context.store(slot.write(), 3);
    final VarInsnNode matchingNode = new VarInsnNode(Opcodes.ISTORE, 3);
    assertTrue(anIStoreTo(slot.read()).test(this.context,matchingNode));

    final VarInsnNode nonMatchingNode = new VarInsnNode(Opcodes.ISTORE, 4);
    assertFalse(anIStoreTo(slot.read()).test(this.context,nonMatchingNode));
  }

  @Test
  public void shouldMatchAgainstCapturedILoadVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    this.context.store(slot.write(), 3);
    final VarInsnNode matchingNode = new VarInsnNode(Opcodes.ILOAD, 3);
    assertTrue(anILoadOf(slot.read()).test(this.context,matchingNode));

    final VarInsnNode nonMatchingNode = new VarInsnNode(Opcodes.ILOAD, 4);
    assertFalse(anILoadOf(slot.read()).test(this.context,nonMatchingNode));
  }

  @Test
  public void shouldMatchAllIntegerConstants() {
    assertFalse(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ACONST_NULL))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_M1))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_0))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_1))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_2))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_3))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_4))));
    assertTrue(anIntegerConstant().test(this.context,(new InsnNode(Opcodes.ICONST_5))));
  }

  @Test
  public void shouldCaptureLabels() {
    final Slot<LabelNode> slot = Slot.create(LabelNode.class);
    final LabelNode label = new LabelNode();
    assertFalse(aLabelNode(slot.write()).test(this.context,new InsnNode(Opcodes.NULL)));
    assertTrue(aLabelNode(slot.write()).test(this.context,label));
    assertThat(this.context.retrieve(slot.read())).isEqualTo(Optional.ofNullable(label));
  }

  @Test
  public void shouldMatchJumps() {
    assertTrue(aJump().test(this.context,new JumpInsnNode(Opcodes.GOTO, null)));
    assertFalse(aJump().test(this.context, new InsnNode(Opcodes.ACONST_NULL)));
  }

  @Test
  public void shouldMatchConditionalJumps() {
    assertFalse(aConditionalJump().test(this.context,new JumpInsnNode(Opcodes.GOTO, null)));
    assertFalse(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.JSR, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFEQ, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFNE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFLT, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFGE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFGT, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFLE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPEQ, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPNE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPLT, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPGE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPGT, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPLE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ACMPEQ, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ACMPNE, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFNULL, null)));
    assertTrue(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFNONNULL, null)));
  }

  @Test
  public void shouldMatchMethodCallByOwnerAndName() {
    final ClassName clazz = ClassName.fromString("clazz");
    assertTrue(methodCallTo(clazz, "name")
        .test(this.context, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "clazz", "name", "desc", true)));
    assertFalse(methodCallTo(clazz, "name")
        .test(this.context, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "clazz", "notName", "desc", true)));
    assertFalse(methodCallTo(clazz, "name")
        .test(this.context, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "notClazz", "name", "desc", true)));
  }
}
