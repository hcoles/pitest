package org.pitest.mutationtest.incremental;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class NullHistoryStoreTest {
  
  private NullHistoryStore testee = new NullHistoryStore();

  @Test
  public void shouldReturnEmptyClassPath() {
    assertTrue(testee.getHistoricClassPath().isEmpty());
  }
  
  @Test
  public void shouldReturnEmptyResults() {
    assertTrue(testee.getHistoricResults().isEmpty());
  }

}
