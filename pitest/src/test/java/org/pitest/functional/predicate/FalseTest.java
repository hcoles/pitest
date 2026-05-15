package org.pitest.functional.predicate;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * @author henry
 *
 */
public class FalseTest {

  @Test
  public void shouldAlwaysBeFalse() {
    assertThat(False.instance().test(null)).isFalse();
  }

}
