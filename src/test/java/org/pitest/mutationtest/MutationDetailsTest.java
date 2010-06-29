package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class MutationDetailsTest {

  @Test
  public void testParsesLineNumber() {
    final MutationDetails testee = new MutationDetails("foo", "foo.java",
        "org.foo:27: xx ", "method");
    assertEquals(27, testee.stackTraceDescription().getLineNumber());
  }

}
