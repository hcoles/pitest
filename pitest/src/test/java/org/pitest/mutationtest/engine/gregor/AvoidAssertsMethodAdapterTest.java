package org.pitest.mutationtest.engine.gregor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.MethodDecoratorTest;

public class AvoidAssertsMethodAdapterTest extends MethodDecoratorTest {

  @Mock
  private MethodMutationContext     context;

  @Mock
  private Label                     label;

  private AvoidAssertsMethodAdapter testee;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    this.testee = new AvoidAssertsMethodAdapter(this.context, this.mv);
  }

  @Test
  public void shouldDisableMutationsWhenAssertionDisabledFlagIsChecked() {
    this.testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled",
        "Z");
    verify(this.context).disableMutations(anyString());
  }

  @Test
  public void shouldEnableMutationsWhenReachLabelOfFirstIFNEAfterCheckingAssertionDisabledFlag() {
    this.testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled",
        "Z");
    this.testee.visitJumpInsn(Opcodes.IFNE, this.label);
    this.testee.visitLabel(this.label);
    verify(this.context).enableMutatations(anyString());
  }

  @Test
  public void shouldNotEnableMutationsWhenNonAssertionCheckLabelReached() {
    final Label anotherLabel = Mockito.mock(Label.class);
    this.testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled",
        "Z");
    this.testee.visitJumpInsn(Opcodes.IFNE, this.label);
    this.testee.visitLabel(anotherLabel);
    verify(this.context, never()).enableMutatations(anyString());
  }

  @Test
  public void shouldNotTryToEnableMutationsWhenIFNEInstructionEncounteredWithoutCheckingAssertionDisabledFlag() {
    this.testee.visitJumpInsn(Opcodes.IFNE, this.label);
    this.testee.visitLabel(this.label);
    verify(this.context, never()).enableMutatations(anyString());
  }

  @Test
  public void shouldOnlyCaptureFirstIFNEEncountered() {
    final Label anotherLabel = Mockito.mock(Label.class);
    this.testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled",
        "Z");
    this.testee.visitJumpInsn(Opcodes.IFNE, this.label);
    this.testee.visitJumpInsn(Opcodes.IFNE, anotherLabel);
    this.testee.visitLabel(this.label);
    verify(this.context).enableMutatations(anyString());
  }

  @Test
  public void shouldDisableMutationsForCodeSettingWhenAssertionDisabledFlagIsSetInStaticInitializer() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
        "desiredAssertionStatus", "()Z", true);
    verify(this.context).disableMutations(anyString());
    this.testee
    .visitFieldInsn(
        Opcodes.PUTSTATIC,
        "org/pitest/mutationtest/engine/gregor/TestGregorMutater$HasAssertStatement",
        "$assertionsDisabled", "Z");
    verify(this.context).enableMutatations(anyString());
  }

  @Test
  public void shouldForwardInterceptedFieldInstructionsToChild() {
    this.testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled",
        "Z");
    verify(this.mv).visitFieldInsn(Opcodes.GETSTATIC, "foo",
        "$assertionsDisabled", "Z");
  }

  @Test
  public void shouldForwardInterceptedVisitLabelInstructionsToChild() {
    this.testee.visitLabel(this.label);
    verify(this.mv).visitLabel(this.label);
  }

  @Test
  public void shouldForwardInterceptedVisitJumpInstructionsToChild() {
    this.testee.visitJumpInsn(Opcodes.IFEQ, this.label);
    verify(this.mv).visitJumpInsn(Opcodes.IFEQ, this.label);
  }

  @Override
  @Test
  public void shouldForwardVisitMethodInsnToChild() {
    this.testee.visitMethodInsn(1, "foo", "bar", "far", true);
    verify(this.mv).visitMethodInsn(1, "foo", "bar", "far", true);
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return this.testee;
  }

}
