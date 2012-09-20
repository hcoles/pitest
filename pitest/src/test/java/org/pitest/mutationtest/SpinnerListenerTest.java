package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

public class SpinnerListenerTest {

  @Test
  public void shouldPrintSpinnerSequence() {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    PrintStream out = new PrintStream(bos);
    SpinnerListener testee = new SpinnerListener(out);
    testee.handleMutationResult(null);
    testee.handleMutationResult(null);
    assertEquals("\u0008/\u0008-",  new String(bos.toByteArray()));
  }

}
