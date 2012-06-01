package org.pitest.mutationtest.execute;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataInputStream;


public class DefaultReporterTest {

  private DefaultReporter testee;
  

  private ByteArrayOutputStream os;
  
  @Before
  public void setUp() {
    os = new ByteArrayOutputStream();
    testee = new DefaultReporter(os);
  }
  
  @Test
  public void shouldSendMutationIdentifierToOutputStream() throws IOException {
    MutationIdentifier mi = new MutationIdentifier("foo",0, "foo");
    testee.describe(mi);
    SafeDataInputStream is = resultToStream();
    assertEquals(Id.DESCRIBE,is.readByte());
    assertEquals(is.read(MutationIdentifier.class), mi);
  }
  
  
  @Test
  public void shouldSendDetectionStatus() throws IOException {
    MutationIdentifier mi = new MutationIdentifier("foo",0, "foo");
    MutationStatusTestPair ms = new MutationStatusTestPair(2, DetectionStatus.KILLED, "foo");
    testee.report(mi, ms);
    SafeDataInputStream is = resultToStream();
    assertEquals(Id.REPORT,is.readByte());
    assertEquals(is.read(MutationIdentifier.class), mi);
    assertEquals(is.read(MutationStatusTestPair.class), ms);
  }

  private SafeDataInputStream resultToStream() {
    SafeDataInputStream is = new SafeDataInputStream(new ByteArrayInputStream(os.toByteArray()));
    return is;
  }
  
  @Test
  public void shouldSendExitCode() {
    testee.done(ExitCode.TIMEOUT);
    SafeDataInputStream is = resultToStream();
    assertEquals(Id.DONE,is.readByte());
    assertEquals(is.readInt(), ExitCode.TIMEOUT.getCode());
  }
}
