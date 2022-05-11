package org.pitest.mutationtest.engine.gregor.blocks;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class ConcreteBlockCounterTest {

  private ConcreteBlockCounter testee;

  @Before
  public void setUp() {
    this.testee = new ConcreteBlockCounter();
  }

  @Test
  public void shouldTrackBlocks() {
    this.testee.registerNewBlock();
    assertEquals(1, this.testee.getCurrentBlock());
    this.testee.registerNewBlock();
    assertEquals(2, this.testee.getCurrentBlock());
  }

}
