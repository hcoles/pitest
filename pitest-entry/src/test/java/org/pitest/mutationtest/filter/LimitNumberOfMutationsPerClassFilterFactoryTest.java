package org.pitest.mutationtest.filter;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LimitNumberOfMutationsPerClassFilterFactoryTest {

  private final LimitNumberOfMutationsPerClassFilterFactory testee = new LimitNumberOfMutationsPerClassFilterFactory();

  @Test
  public void shouldFilterWhenNumberOfMutationsPerClassGreaterThanThanZero() {
    assertTrue(this.testee.createFilter(null, null, 1) instanceof LimitNumberOfMutationPerClassFilter);
  }

  @Test
  public void shouldNotFilterWhenNumberOfMutationsPerClassIsZero() {
    assertTrue(this.testee.createFilter(null, null, 0) instanceof UnfilteredMutationFilter);
  }

  @Test
  public void shouldNotFilterWhenNumberOfMutationsPerClassLessThanZero() {
    assertTrue(this.testee.createFilter(null, null, -1) instanceof UnfilteredMutationFilter);
  }
}
