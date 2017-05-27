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
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitTableSwitchInsn(0, 1, label);
    verify(this.context).disableMutations(anyString());
  }

  @Test
  public void shouldDisableMutationsWhenLookupSwitchImmediatelyFollowsHashCode() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitLookupSwitchInsn(label, new int[0], new Label[0]);
    verify(this.context).disableMutations(anyString());
  }
  
  @Test
  public void shouldNotDisableMutationsWhenJumpOccursBeforeSwitch() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitJumpInsn(Opcodes.IFEQ, label);
    testee.visitLookupSwitchInsn(label, new int[0], new Label[0]);
    verify(this.context, never()).disableMutations(anyString());
  }
  
  @Test
  public void shouldNotDisableMutationsWhenPushOccursBeforeSwitch() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitIntInsn(Opcodes.BIPUSH, 0);
    testee.visitLookupSwitchInsn(label, new int[0], new Label[0]);
    verify(this.context, never()).disableMutations(anyString());
  }

  
  @Test
  public void shouldNotDisableMutationsWhenIConstOccursBeforeSwitch() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitInsn(Opcodes.ICONST_0);
    testee.visitLookupSwitchInsn(label, new int[0], new Label[0]);
    verify(this.context, never()).disableMutations(anyString());
  }
  
  @Test
  public void shouldRenableMutationWhenSwitchDefaultHit() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitTableSwitchInsn(0, 1, label);
    testee.visitLabel(label);
    verify(this.context).enableMutatations(anyString());
  }
  
  @Test
  public void shouldNotRenableMutationWhenDifferentLabelHit() {
    testee.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/String", "hashCode", "unused", false);
    testee.visitTableSwitchInsn(0, 1, label);
    testee.visitLabel(Mockito.mock(Label.class));
    verify(this.context, never()).enableMutatations(anyString());
  }
  
  @Test
  public void shouldRenableMutationsWhenMethodVisitEnds() {
    testee.visitEnd();
    verify(this.context).enableMutatations(anyString());
  }
  
  @Override
  protected MethodVisitor getTesteeVisitor() {
    return testee;
  }

}
