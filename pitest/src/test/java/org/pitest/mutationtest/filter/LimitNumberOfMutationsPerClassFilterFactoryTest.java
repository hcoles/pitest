package org.pitest.mutationtest.filter;

import static org.junit.Assert.*;

import org.junit.Test;

public class LimitNumberOfMutationsPerClassFilterFactoryTest {
  
  private LimitNumberOfMutationsPerClassFilterFactory testee = new LimitNumberOfMutationsPerClassFilterFactory();

  @Test
  public void shouldFilterWhenNumberOfMutationsPerClassGreaterThanThanZero() {
    assertTrue(testee.createFilter(null, 1, null) instanceof LimitNumberOfMutationPerClassFilter);
  }
  
  @Test
  public void shouldNotFilterWhenNumberOfMutationsPerClassIsZero() {
    assertTrue(testee.createFilter(null, 0, null) instanceof UnfilteredMutationFilter);
  }

  @Test
  public void shouldNotFilterWhenNumberOfMutationsPerClassLessThanZero() {
    assertTrue(testee.createFilter(null, -1, null) instanceof UnfilteredMutationFilter);
  }
}
