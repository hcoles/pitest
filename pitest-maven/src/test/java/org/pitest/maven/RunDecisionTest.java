package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class RunDecisionTest {

  @Test
  public void shouldRunIfThereAreNoReasonsWhyNot() {
    PitMojo.RunDecision rd = new PitMojo.RunDecision();

    assertThat(rd.getReasons()).isEmpty();
    assertThat(rd.shouldRun()).isTrue();
  }

  @Test
  public void shouldNotRunIfThereIsAReasonsWhyNot() {
    PitMojo.RunDecision rd = new PitMojo.RunDecision();

    rd.addReason("Today is Sunday");

    assertThat(rd.getReasons()).hasSize(1);
    assertThat(rd.shouldRun()).isFalse();
  }

  @Test
  public void addReasonAddsReason() {
    PitMojo.RunDecision rd = new PitMojo.RunDecision();

    rd.addReason("Today is Sunday");

    assertThat(rd.getReasons()).hasSize(1);
  }

  @Test
  public void getReasonsReturnsAllReasons() {
    PitMojo.RunDecision rd = new PitMojo.RunDecision();

    rd.addReason("Today is Monday");
    rd.addReason("Today is Sunday");

    assertThat(rd.getReasons()).hasSize(2);
    assertThat(rd.getReasons()).contains("Today is Sunday", "Today is Monday");
  }

}