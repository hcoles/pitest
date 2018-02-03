package org.pitest.mutationtest.build.intercept.kotlin;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class KotlinFilterFactoryTest {

  KotlinFilterFactory testee = new KotlinFilterFactory();

  @Test
  public void shouldBeOnByDefault() {
    assertThat(this.testee.provides().isOnByDefault()).isTrue();
  }

}
