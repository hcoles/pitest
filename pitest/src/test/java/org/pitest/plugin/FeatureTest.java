package org.pitest.plugin;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

import static org.assertj.core.api.Assertions.assertThat;

public class FeatureTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(Feature.class).verify();
  }
  
  @Test
  public void shouldUseOnlyNameForEquality() {
    assertThat(Feature.named("foo"))
    .isEqualTo(Feature.named("foo").withDescription("?").withOnByDefault(false));
  }

}
