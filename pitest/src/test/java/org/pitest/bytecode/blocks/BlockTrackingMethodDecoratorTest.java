package org.pitest.bytecode.blocks;

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
  public void shouldRegisiterNewBlockForJumpInstructions() {
    testee.visitJumpInsn(0,null);
    verify(this.tracker).registerNewBlock();
  }
  
  @Test
  public void shouldRegisiterNewBlockForReturnInstructions() {
    testee.visitInsn(RETURN);
    verify(this.tracker).registerNewBlock();
  }
  
  @Test
  public void shouldRegisiterNewBlockForAReturnInstructions() {
    testee.visitInsn(ARETURN);
    verify(this.tracker).registerNewBlock();
  }
  
  @Test
  public void shouldRegisiterNewBlockForFReturnInstructions() {
    testee.visitInsn(FRETURN);
    verify(this.tracker).registerNewBlock();
  }  
  
  @Test
  public void shouldRegisiterNewBlockForIReturnInstructions() {
    testee.visitInsn(IRETURN);
    verify(this.tracker).registerNewBlock();
  }  
  
  @Test
  public void shouldRegisiterNewBlockForLReturnInstructions() {
    testee.visitInsn(LRETURN);
    verify(this.tracker).registerNewBlock();
  } 
  
  @Test
  public void shouldRegisiterNewBlockForAThrowInstructions() {
    testee.visitInsn(ATHROW); // possible without also getting a jump??
    verify(this.tracker).registerNewBlock();
  }   
  
  @Test
  public void shouldNotRegisterANewBlockForOtherInsn() {
    testee.visitInsn(ICONST_0); 
    verify(this.tracker, never()).registerNewBlock();
  } 
  
  @Override
  protected MethodVisitor getTesteeVisitor() {
    return testee;
  }

}
