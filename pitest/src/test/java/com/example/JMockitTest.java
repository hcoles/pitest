package com.example;

import mockit.Mocked;
import mockit.Verifications;

import org.junit.Test;

public final class JMockitTest {

  @Mocked
  CoveredByJMockit.AnInterface mock;

  @Test
  // Uses of JMockit API: 1
  public void verifyBehavior() {

    CoveredByJMockit.doStuff(this.mock);

    // Invocations to mock are verified (verify phase):
    new Verifications() {
      {
        JMockitTest.this.mock.callMe();
        JMockitTest.this.mock.callMe();
      }
    };

  }

}
