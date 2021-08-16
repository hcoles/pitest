package org.pitest.mutationtest.engine.gregor.blocks;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;
import static org.objectweb.asm.Opcodes.NOP;
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
    this.testee = new BlockTrackingMethodDecorator(this.tracker, this.mv, 0,
        "foo", "(II)V", null, null);
  }

  @Test
  public void shouldRegisiterNewBlockForJumpInstructions() {
    this.testee.visitJumpInsn(0, new Label());
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(ARETURN);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForReturnInstructions() {
    this.testee.visitInsn(RETURN);
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(RETURN);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForAReturnInstructions() {
    this.testee.visitInsn(ARETURN);
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(ARETURN);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForFReturnInstructions() {
    this.testee.visitInsn(FRETURN);
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(FRETURN);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForIReturnInstructions() {
    this.testee.visitInsn(IRETURN);
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(IRETURN);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForLReturnInstructions() {
    this.testee.visitInsn(LRETURN);
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(LRETURN);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldRegisterNewBlockForAThrowInstructions() {
    this.testee.visitInsn(ATHROW); // possible without also getting a jump??
    this.testee.visitInsn(NOP);
    this.testee.visitInsn(ATHROW);
    this.testee.visitEnd();
    verify(this.tracker).registerNewBlock();
  }

  @Test
  public void shouldNotRegisterANewBlockForOtherInsn() {
    this.testee.visitInsn(ICONST_0);
    verify(this.tracker, never()).registerNewBlock();
  }

  @Override
  protected MethodVisitor getTesteeVisitor() {
    return this.testee;
  }

}
