package org.pitest.internal;

import org.junit.Test;

public class IsolationUtilsTest {

  @Test
  public void shouldSerializeAndDeserializeForTransport() {

    final String encodedXml = IsolationUtils.toTransportString(new ClassPath());
    IsolationUtils.fromTransportString(encodedXml);
    // pass if get here without error
  }

}
