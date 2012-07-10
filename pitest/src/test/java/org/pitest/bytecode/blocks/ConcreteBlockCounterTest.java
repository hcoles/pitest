package org.pitest.bytecode.blocks;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;


public class ConcreteBlockCounterTest {
  
  private ConcreteBlockCounter testee;
  
  @Before
  public void setUp() {
    testee = new ConcreteBlockCounter();
  }
  
  @Test
  public void shouldTrackBlocks() {
    testee.registerNewBlock();
    assertEquals(1,testee.getCurrentBlock());
    testee.registerNewBlock();
    assertEquals(2,testee.getCurrentBlock());
  }
  
  @Test
  public void shouldTrackWhenCodeIsWithinFinallyBlocks() {
    assertFalse(testee.isWithinExceptionHandler());
    testee.registerFinallyBlockStart();
    assertTrue(testee.isWithinExceptionHandler());
    testee.registerFinallyBlockEnd();
    assertFalse(testee.isWithinExceptionHandler());
  }

}
