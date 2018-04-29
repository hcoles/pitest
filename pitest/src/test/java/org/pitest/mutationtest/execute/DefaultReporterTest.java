package org.pitest.mutationtest.execute;

import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.LocationMother.aMutationId;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.ExitCode;
import org.pitest.util.Id;
import org.pitest.util.SafeDataInputStream;

public class DefaultReporterTest {

  private DefaultReporter       testee;

  private ByteArrayOutputStream os;

  @Before
  public void setUp() {
    this.os = new ByteArrayOutputStream();
    this.testee = new DefaultReporter(this.os);
  }

  @Test
  public void shouldSendMutationIdentifierToOutputStream() throws IOException {
    final MutationIdentifier mi = aMutationId().withIndex(0).withMutator("foo")
        .build();
    this.testee.describe(mi);
    final SafeDataInputStream is = resultToStream();
    assertEquals(Id.DESCRIBE, is.readByte());
    assertEquals(is.read(MutationIdentifier.class), mi);
  }

  @Test
  public void shouldSendDetectionStatus() throws IOException {
    final MutationIdentifier mi = aMutationId().withIndex(0).withMutator("foo")
        .build();
    final MutationStatusTestPair ms = new MutationStatusTestPair(2,
        DetectionStatus.KILLED, "foo");
    this.testee.report(mi, ms);
    final SafeDataInputStream is = resultToStream();
    assertEquals(Id.REPORT, is.readByte());
    assertEquals(is.read(MutationIdentifier.class), mi);
    assertEquals(is.read(MutationStatusTestPair.class), ms);
  }

  private SafeDataInputStream resultToStream() {
    final SafeDataInputStream is = new SafeDataInputStream(
        new ByteArrayInputStream(this.os.toByteArray()));
    return is;
  }

  @Test
  public void shouldSendExitCode() {
    this.testee.done(ExitCode.TIMEOUT);
    final SafeDataInputStream is = resultToStream();
    assertEquals(Id.DONE, is.readByte());
    assertEquals(is.readInt(), ExitCode.TIMEOUT.getCode());
  }

}
