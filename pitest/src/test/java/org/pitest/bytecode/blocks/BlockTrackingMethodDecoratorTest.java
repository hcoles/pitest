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
    Label end = new Label();
    Label start = new Label();
    Label handler = new Label();
    testee.visitTryCatchBlock(start, end, handler, null);
    testee.visitLabel(handler);
    verify(tracker).registerFinallyBlockStart();
  }
  
  @Test
  public void shouldNotRegisterFinallyBlockStartWhenHitsLabelFromNonNullExceptionHandler() {
    Label end = new Label();
    Label start = new Label();
    Label handler = new Label();
    testee.visitTryCatchBlock(start, end, handler, "NotNull");
    testee.visitLabel(handler);
    verify(tracker,never()).registerFinallyBlockStart();
  }
  
  @Test
  public void shouldNotRegisterFinallyBlockStartWhenHitsOtherLabelsFromNonNullExceptionHandler() {
    Label label = new Label();
    testee.visitLabel(label);
    verify(tracker,never()).registerFinallyBlockStart();
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
  public void shouldRegisiterFinallyBlockEndForReturnInstructions() {
    testee.visitInsn(RETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }
  
  @Test
  public void shouldRegisiterNewBlockForAReturnInstructions() {
    testee.visitInsn(ARETURN);
    verify(this.tracker).registerNewBlock();
  }
  
  @Test
  public void shouldRegisiterFinallyBlockEndForAReturnInstructions() {
    testee.visitInsn(ARETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }
  
  @Test
  public void shouldRegisiterNewBlockForFReturnInstructions() {
    testee.visitInsn(FRETURN);
    verify(this.tracker).registerNewBlock();
  } 
  
  @Test
  public void shouldRegisiterFinallyBlockEndForFReturnInstructions() {
    testee.visitInsn(FRETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }
  
  @Test
  public void shouldRegisiterNewBlockForIReturnInstructions() {
    testee.visitInsn(IRETURN);
    verify(this.tracker).registerNewBlock();
  }  
  
  @Test
  public void shouldRegisiterFinallyBlockEndForIReturnInstructions() {
    testee.visitInsn(IRETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }
  
  @Test
  public void shouldRegisiterNewBlockForLReturnInstructions() {
    testee.visitInsn(LRETURN);
    verify(this.tracker).registerNewBlock();
  } 
  
  @Test
  public void shouldRegisiterFinallyBlockEndForLReturnInstructions() {
    testee.visitInsn(LRETURN);
    verify(this.tracker).registerFinallyBlockEnd();
  }
  
  @Test
  public void shouldRegisiterNewBlockForAThrowInstructions() {
    testee.visitInsn(ATHROW); // possible without also getting a jump??
    verify(this.tracker).registerNewBlock();
  }   
  
  @Test
  public void shouldRegisiterFinallyBlockEndForAThrowInstructions() {
    testee.visitInsn(ATHROW);
    verify(this.tracker).registerFinallyBlockEnd();
  }
  
  @Test
  public void shouldNotRegisterANewBlockForOtherInsn() {
    testee.visitInsn(ICONST_0); 
    verify(this.tracker, never()).registerNewBlock();
  }
  
  @Test
  public void shouldNotRegisterFinallyBlockEndForOtherInsn() {
    testee.visitInsn(ICONST_0); 
    verify(this.tracker, never()).registerFinallyBlockEnd();
  } 
  
  @Override
  protected MethodVisitor getTesteeVisitor() {
    return testee;
  }

}
