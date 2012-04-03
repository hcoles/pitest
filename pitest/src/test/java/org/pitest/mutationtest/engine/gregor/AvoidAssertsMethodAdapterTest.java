package org.pitest.mutationtest.engine.gregor;

import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class AvoidAssertsMethodAdapterTest {

  @Mock
  private Context                   context;

  @Mock
  private MethodVisitor             child;

  @Mock
  private Label                     label;

  private AvoidAssertsMethodAdapter testee;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new AvoidAssertsMethodAdapter(context, child);
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
  public void shouldForwardInterceptedFieldInstructionsToChild() {
    testee.visitFieldInsn(Opcodes.GETSTATIC, "foo", "$assertionsDisabled", "Z");
    verify(child).visitFieldInsn(Opcodes.GETSTATIC, "foo",
        "$assertionsDisabled", "Z");
  }

  @Test
  public void shouldForwardInterceptedVisitLabelInstructionsToChild() {
    testee.visitLabel(label);
    verify(child).visitLabel(label);
  }

  @Test
  public void shouldForwardInterceptedVisitJumpInstructionsToChild() {
    testee.visitJumpInsn(Opcodes.IFEQ, label);
    verify(child).visitJumpInsn(Opcodes.IFEQ, label);
  }

}
