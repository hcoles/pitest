package org.pitest.coverage.execute;

import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.pitest.util.Log;
import org.pitest.util.SafeDataOutputStream;

final class SendData implements Consumer<SafeDataOutputStream> {
  private static final Logger   LOG = Log.getLogger();
  private final CoverageOptions arguments;
  private final List<String>    testClasses;

  SendData(final CoverageOptions arguments, final List<String> testClasses) {
    this.arguments = arguments;
    this.testClasses = testClasses;
  }

  @Override
  public void accept(final SafeDataOutputStream dos) {
    sendArguments(dos);
    sendTests(dos);
  }

  private void sendArguments(final SafeDataOutputStream dos) {
    dos.write(this.arguments);
    dos.flush();
  }

  private void sendTests(final SafeDataOutputStream dos) {

    // send individually to reduce memory overhead of deserializing large
    // suite
    LOG.info("Sending " + this.testClasses.size() + " test classes to minion");
    dos.writeInt(this.testClasses.size());
    for (final String tc : this.testClasses) {
      dos.writeString(tc);
    }
    dos.flush();
    LOG.info("Sent tests to minion");

  }
}
