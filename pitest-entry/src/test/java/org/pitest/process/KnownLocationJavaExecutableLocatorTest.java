package org.pitest.process;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KnownLocationJavaExecutableLocatorTest {

  @Test
  public void shouldReturnWrappedLocation() {
    assertEquals("foo",
        new KnownLocationJavaExecutableLocator("foo").javaExecutable());
  }

}
