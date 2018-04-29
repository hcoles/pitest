package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NullHistoryStoreTest {

  private final NullHistoryStore testee = new NullHistoryStore();

  @Test
  public void shouldReturnEmptyClassPath() {
    assertTrue(this.testee.getHistoricClassPath().isEmpty());
  }

  @Test
  public void shouldReturnEmptyResults() {
    assertTrue(this.testee.getHistoricResults().isEmpty());
  }

}
