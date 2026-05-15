package org.pitest.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.pitest.mutationtest.MutationEngineFactory;

public class ServiceLoaderTest {

  public static interface AService {

  }

  @Test
  public void shouldReturnNoValuesWhenNoServicesFounds() throws Exception {
    final Iterable<AService> actual = ServiceLoader.load(AService.class, Thread
        .currentThread().getContextClassLoader());
    assertThat(actual).isNullOrEmpty();
  }

  @Test
  public void shouldReturnValueWhenServiceFound() throws Exception {
    final Iterable<MutationEngineFactory> actual = ServiceLoader.load(
        MutationEngineFactory.class, Thread.currentThread()
        .getContextClassLoader());
    assertThat(actual).isNotEmpty();
  }

}
