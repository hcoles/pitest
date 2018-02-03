package org.pitest.mutationtest.filter;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class LimitNumberOfMutationsPerClassFilterFactoryTest {

  private final LimitNumberOfMutationsPerClassFilterFactory testee = new LimitNumberOfMutationsPerClassFilterFactory();

  @Test
  public void shouldBeOffByDefault() {
    assertThat(this.testee.provides().isOnByDefault()).isFalse();
  }

}
