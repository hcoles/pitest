package org.pitest.mutationtest.engine.gregor.blocks;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.RETURN;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.MethodDecoratorTest;

public class BlockTrackingMethodDecoratorTest extends MethodDecoratorTest {

  private BlockTrackingMethodDecorator testee;

  @Mock
  private BlockCounter                 tracker;

  @Override
  @Before
  public void setUp() {
    super.setUp();
    this.testee = new BlockTrackingMethodDecorator(this.tracker, this.mv);
  }

  @Test
  public void shouldRegisterFinallyBlockStartWhenHitsLabelFromNullExceptionHandler() {
    final Label end = new Label();
    final Label start = new Label();
    final Label handler = new Label();
    this.testee.visitTryCatchBlock(start, end, handler, null);
    this.testee.visitLabel(handler);
    verify(this.tracker).registerFinallyBlockStart();
  }

  @Test
  public void shouldNotRegisterFinallyBlockStartWhenHitsLabelFromNonNullExceptionHandler() {
    final Label end = new Label();
    final Label start = new Label();
    final Label handler = new Label();
    this.testee.visitTryCatchBlock(start, end, handler, "NotNull");
    this.testee.visitLabel(handler);
    verify(this.tracker, never()).registerFinallyBlockStart();
  }

  @Test
  public void shouldNotRegisterFinallyBlockStartWhenHitsOtherLabelsFromNonNullExceptionHandler() {
    final Label label = new Label();
    this.testee.visitLabel(label);
    verify(this.tracker, never()).registerFinallyBlockStart();
  }

  @Test
  public void shouldRegisiterNewBlockForJumpInstructions() {
    this.testee.visitJumpInsn(0, null);
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForReturnInstructions() {
    this.testee.visitInsn(RETURN);
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterFinallyBlockEndForReturnInstructions() {
    this.testee.visitInsn(RETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }

  @Test
  public void shouldRegisterNewBlockForAReturnInstructions() {
    this.testee.visitInsn(ARETURN);
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterFinallyBlockEndForAReturnInstructions() {
    this.testee.visitInsn(ARETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }

  @Test
  public void shouldRegisterNewBlockForFReturnInstructions() {
    this.testee.visitInsn(FRETURN);
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterFinallyBlockEndForFReturnInstructions() {
    this.testee.visitInsn(FRETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }

  @Test
  public void shouldRegisterNewBlockForIReturnInstructions() {
    this.testee.visitInsn(IRETURN);
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterFinallyBlockEndForIReturnInstructions() {
    this.testee.visitInsn(IRETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }

  @Test
  public void shouldRegisterNewBlockForLReturnInstructions() {
    this.testee.visitInsn(LRETURN);
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterFinallyBlockEndForLReturnInstructions() {
    this.testee.visitInsn(LRETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }

  @Test
  public void shouldRegisterNewBlockForAThrowInstructions() {
    this.testee.visitInsn(ATHROW); // possible without also getting a jump??
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterFinallyBlockEndForAThrowInstructions() {
    this.testee.visitInsn(ATHROW);
    verify(this.tracker).registerFinallyBlockEnd();
  }

  @Test
  public void shouldNotRegisterANewBlockForOtherInsn() {
    this.testee.visitInsn(ICONST_0);
    verify(this.tracker, never()).registerNewBlock();
  }

  @Test
  public void shouldNotRegisterFinallyBlockEndForOtherInsn() {
    this.testee.visitInsn(ICONST_0);
    verify(this.tracker, never()).registerFinallyBlockEnd();
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return this.testee;
  }

}
