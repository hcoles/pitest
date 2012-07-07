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
  private Context                   context;

  @Mock
  private Label                     label;

  private AvoidAssertsMethodAdapter testee;

  @Before
  public void setUp() {
    super.setUp();
    testee = new AvoidAssertsMethodAdapter(context, this.mv);
  }

  @Test
  public void shouldDisableMutationsWhenAssertionDisabledFlagIsChecked() {
    testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled", "Z");
    verify(context).disableMutations(anyString());
  }

  @Test
  public void shouldEnableMutationsWhenReachLabelOfFirstIFNEAfterCheckingAssertionDisabledFlag() {
    testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled", "Z");
    testee.visitJumpInsn(Opcodes.IFNE, label);
    testee.visitLabel(label);
    verify(context).enableMutatations(anyString());
  }

  @Test
  public void shouldNotEnableMutationsWhenNonAssertionCheckLabelReached() {
    Label anotherLabel = Mockito.mock(Label.class);
    testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled", "Z");
    testee.visitJumpInsn(Opcodes.IFNE, label);
    testee.visitLabel(anotherLabel);
    verify(context, never()).enableMutatations(anyString());
  }

  @Test
  public void shouldNotTryToEnableMutationsWhenIFNEInstructionEncounteredWithoutCheckingAssertionDisabledFlag() {
    testee.visitJumpInsn(Opcodes.IFNE, label);
    testee.visitLabel(label);
    verify(context, never()).enableMutatations(anyString());
  }

  @Test
  public void shouldOnlyCaptureFirstIFNEEncountered() {
    Label anotherLabel = Mockito.mock(Label.class);
    testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled", "Z");
    testee.visitJumpInsn(Opcodes.IFNE, label);
    testee.visitJumpInsn(Opcodes.IFNE, anotherLabel);
    testee.visitLabel(label);
    verify(context).enableMutatations(anyString());
  }
  
  @Test
  public void shouldDisableMutationsForCodeSettingWhenAssertionDisabledFlagIsSetInStaticInitializer() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class", "desiredAssertionStatus", "()Z");
    verify(context).disableMutations(anyString());
    testee.visitFieldInsn(Opcodes.PUTSTATIC, "org/pitest/mutationtest/engine/gregor/TestGregorMutater$HasAssertStatement", "$assertionsDisabled", "Z");
    verify(context).enableMutatations(anyString());
  }

  @Test
  public void shouldForwardInterceptedFieldInstructionsToChild() {
    testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled", "Z");
    verify(mv).visitFieldInsn(Opcodes.GETSTATIC, "foo",
        "$assertionsDisabled", "Z");
  }

  @Test
  public void shouldForwardInterceptedVisitLabelInstructionsToChild() {
    testee.visitLabel(label);
    verify(mv).visitLabel(label);
  }

  @Test
  public void shouldForwardInterceptedVisitJumpInstructionsToChild() {
    testee.visitJumpInsn(Opcodes.IFEQ, label);
    verify(mv).visitJumpInsn(Opcodes.IFEQ, label);
  }

  @Test
  public void shouldForwardVisitMethodInsnToChild() {
    testee.visitMethodInsn(1, "foo", "bar", "far");
    verify(mv).visitMethodInsn(1, "foo", "bar", "far");
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return testee;
  }
  
}
