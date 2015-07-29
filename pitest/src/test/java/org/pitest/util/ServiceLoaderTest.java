package org.pitest.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.pitest.mutationtest.MutationEngineFactory;

public class ServiceLoaderTest {

  public static interface AService {

  }

  @Test
  public void shouldReturnNoValuesWhenNoServicesFounds() throws Exception {
    final Iterable<AService> actual = ServiceLoader.load(AService.class, Thread
        .currentThread().getContextClassLoader());
    assertFalse(actual.iterator().hasNext());
  }

  @Test
  public void shouldReturnValueWhenServiceFound() throws Exception {
    final Iterable<MutationEngineFactory> actual = ServiceLoader.load(
        MutationEngineFactory.class, Thread.currentThread()
        .getContextClassLoader());
    assertTrue(actual.iterator().hasNext());
  }

}
