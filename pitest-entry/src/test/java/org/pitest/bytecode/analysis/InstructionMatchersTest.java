package org.pitest.bytecode.analysis;

import static org.assertj.core.api.Assertions.assertThat;
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

import org.assertj.core.api.AbstractBooleanAssert;
import org.checkerframework.checker.nullness.qual.NonNull;
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
import org.pitest.sequence.Result;
import org.pitest.sequence.Slot;

public class InstructionMatchersTest {

  private Context context = Context.start();

  @Test
  public void anyInstructionShouldMatchAnything() {
    final AbstractInsnNode node = new InsnNode(-1);
    assertThat(anyInstruction().test(this.context, node).result()).isTrue();
  }

  @Test
  public void opCodeShouldMatchOnOpcode() {
    final AbstractInsnNode node = new InsnNode(-1);
    assertThat(opCode(-1).test(this.context, node).result()).isTrue();
    assertThat(opCode(0).test(this.context, node).result()).isFalse();
  }

  @Test
  public void isAShouldMatchOnType() {
    final AbstractInsnNode node = new InsnNode(-1);
    assertThat(isA(InsnNode.class).test(this.context, node).result()).isTrue();
    assertThat(isA(LabelNode.class).test(this.context, node).result()).isFalse();
  }

  @Test
  public void shouldMatchIncrementsToStoredLocalVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    context = context.store(slot.write(), 42);
    final IincInsnNode node = new IincInsnNode(42, 1);
    assertThat(incrementsVariable(slot.read()).test(context, node).result()).isTrue();
  }

  @Test
  public void shouldNotMatchIncrementsToDifferentLocalVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    this.context.store(slot.write(), 42);
    final IincInsnNode node = new IincInsnNode(42 + 1, 1);
    assertThat(incrementsVariable(slot.read()).test(this.context, node).result()).isFalse();
  }

  @Test
  public void shouldCaptureIStoreVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    final VarInsnNode node = new VarInsnNode(Opcodes.ISTORE, 3);
    Result result = anIStore(slot.write()).test(this.context, node);
    assertThat(result.result()).isTrue();
    assertThat(result.context().retrieve(slot.read())).isEqualTo(Optional.ofNullable(3));
  }

  @Test
  public void shouldMatchAgainstCapturedIStoreVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    context = context.store(slot.write(), 3);
    final VarInsnNode matchingNode = new VarInsnNode(Opcodes.ISTORE, 3);
    assertThat(anIStoreTo(slot.read()).test(context, matchingNode).result()).isTrue();

    final VarInsnNode nonMatchingNode = new VarInsnNode(Opcodes.ISTORE, 4);
    assertThat(anIStoreTo(slot.read()).test(context, nonMatchingNode).result()).isFalse();
  }

  @Test
  public void shouldMatchAgainstCapturedILoadVariable() {
    final Slot<Integer> slot = Slot.create(Integer.class);
    this.context = this.context.store(slot.write(), 3);
    final VarInsnNode matchingNode = new VarInsnNode(Opcodes.ILOAD, 3);
    assertThat(anILoadOf(slot.read()).test(this.context, matchingNode).result()).isTrue();

    final VarInsnNode nonMatchingNode = new VarInsnNode(Opcodes.ILOAD, 4);
    assertThat(anILoadOf(slot.read()).test(this.context, nonMatchingNode).result()).isFalse();
  }

  @Test
  public void shouldMatchAllIntegerConstants() {
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ACONST_NULL))).result()).isFalse();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_M1))).result()).isTrue();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_0))).result()).isTrue();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_1))).result()).isTrue();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_2))).result()).isTrue();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_3))).result()).isTrue();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_4))).result()).isTrue();
    assertThat(anIntegerConstant().test(this.context, (new InsnNode(Opcodes.ICONST_5))).result()).isTrue();
  }

  @Test
  public void shouldCaptureLabels() {
    final Slot<LabelNode> slot = Slot.create(LabelNode.class);
    final LabelNode label = new LabelNode();
    assertThat(aLabelNode(slot.write()).test(this.context, new InsnNode(Opcodes.NULL)).result()).isFalse();

    Result result = aLabelNode(slot.write()).test(this.context, label);
    assertThat(result.result()).isTrue();
    assertThat(result.context().retrieve(slot.read())).isEqualTo(Optional.ofNullable(label));
  }

  @Test
  public void shouldMatchJumps() {
    assertThat(aJump().test(this.context, new JumpInsnNode(Opcodes.GOTO, null)).result()).isTrue();
    assertThat(aJump().test(this.context, new InsnNode(Opcodes.ACONST_NULL)).result()).isFalse();
  }

  @Test
  public void shouldMatchConditionalJumps() {
    resultFor(Opcodes.GOTO).isFalse();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.JSR, null)).result()).isFalse();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFEQ, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFNE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFLT, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFGE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFGT, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFLE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPEQ, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPNE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPLT, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPGE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPGT, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ICMPLE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ACMPEQ, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IF_ACMPNE, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFNULL, null)).result()).isTrue();
    assertThat(aConditionalJump().test(this.context, new JumpInsnNode(Opcodes.IFNONNULL, null)).result()).isTrue();
  }

  private @NonNull AbstractBooleanAssert<?> resultFor(int instruction) {
    return assertThat(aConditionalJump().test(this.context, new JumpInsnNode(instruction, null)).result());
  }

  @Test
  public void shouldMatchMethodCallByOwnerAndName() {
    final ClassName clazz = ClassName.fromString("clazz");
    assertThat(methodCallTo(clazz, "name")
            .test(this.context, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "clazz", "name", "desc", true)).result()).isTrue();
    assertThat(methodCallTo(clazz, "name")
            .test(this.context, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "clazz", "notName", "desc", true)).result()).isFalse();
    assertThat(methodCallTo(clazz, "name")
            .test(this.context, new MethodInsnNode(Opcodes.INVOKEINTERFACE, "notClazz", "name", "desc", true)).result()).isFalse();
  }
}
