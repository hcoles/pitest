package com.example.coverage.execute.samples.simple;

import org.junit.Test;

public class TesteeThatIsAnInterfaceTest {

  @Test
  public void testMethodRuns() {
    new TesteeThatIsAnInterface(){}.foo();
  }

}
