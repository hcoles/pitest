package org.pitest.functional.predicate;

import static org.pitest.functional.prelude.Prelude.not;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author henry
 *
 */
public class NotTest {

  @Test
  public void shouldInvertTrue() {
    assertThat(not(i -> true).test(null)).isFalse();
  }

  @Test
  public void shouldInvertFalse() {
    assertThat(not(False.instance()).test(null)).isTrue();
  }
}
