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

public class AvoidStringSwitchedMethodAdapterTest extends MethodDecoratorTest {

  @Mock
  private MethodMutationContext     context;

  @Mock
  private Label                     label;

  private AvoidStringSwitchedMethodAdapter testee;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    this.testee = new AvoidStringSwitchedMethodAdapter(this.context, this.mv);
  }

  @Test
  public void shouldDisableMutationsWhenTableSwitchImmediatelyFollowsHashCode() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitTableSwitchInsn(0, 1, this.label);
    verify(this.context).disableMutations(anyString());
  }

  @Test
  public void shouldDisableMutationsWhenLookupSwitchImmediatelyFollowsHashCode() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitLookupSwitchInsn(this.label, new int[0], new Label[0]);
    verify(this.context).disableMutations(anyString());
  }

  @Test
  public void shouldNotDisableMutationsWhenJumpOccursBeforeSwitch() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitJumpInsn(Opcodes.IFEQ, this.label);
    this.testee.visitLookupSwitchInsn(this.label, new int[0], new Label[0]);
    verify(this.context, never()).disableMutations(anyString());
  }

  @Test
  public void shouldNotDisableMutationsWhenPushOccursBeforeSwitch() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitIntInsn(Opcodes.BIPUSH, 0);
    this.testee.visitLookupSwitchInsn(this.label, new int[0], new Label[0]);
    verify(this.context, never()).disableMutations(anyString());
  }


  @Test
  public void shouldNotDisableMutationsWhenIConstOccursBeforeSwitch() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitInsn(Opcodes.ICONST_0);
    this.testee.visitLookupSwitchInsn(this.label, new int[0], new Label[0]);
    verify(this.context, never()).disableMutations(anyString());
  }

  @Test
  public void shouldRenableMutationWhenSwitchDefaultHit() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitTableSwitchInsn(0, 1, this.label);
    this.testee.visitLabel(this.label);
    verify(this.context).enableMutatations(anyString());
  }

  @Test
  public void shouldNotRenableMutationWhenDifferentLabelHit() {
    this.testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    this.testee.visitTableSwitchInsn(0, 1, this.label);
    this.testee.visitLabel(Mockito.mock(Label.class));
    verify(this.context, never()).enableMutatations(anyString());
  }

  @Test
  public void shouldRenableMutationsWhenMethodVisitEnds() {
    this.testee.visitEnd();
    verify(this.context).enableMutatations(anyString());
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return this.testee;
  }

}
