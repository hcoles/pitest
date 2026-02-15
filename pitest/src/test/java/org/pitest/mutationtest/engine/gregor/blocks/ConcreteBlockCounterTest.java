package org.pitest.mutationtest.engine.gregor.blocks;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(this.testee.getCurrentBlock()).isEqualTo(1);
    this.testee.registerNewBlock();
    assertThat(this.testee.getCurrentBlock()).isEqualTo(2);
  }

}
