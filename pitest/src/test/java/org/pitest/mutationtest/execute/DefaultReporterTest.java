package org.pitest.mutationtest.execute;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.util.ExitCode;
import org.pitest.util.Id;
import org.pitest.util.SafeDataInputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.mutationtest.LocationMother.aMutationId;

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
    assertThat(is.readByte()).isEqualTo(Id.DESCRIBE);
    assertThat(is.read(MutationIdentifier.class)).isEqualTo(mi);
  }

  @Test
  public void shouldSendDetectionStatus() throws IOException {
    final MutationIdentifier mi = aMutationId().withIndex(0).withMutator("foo")
        .build();
    final MutationStatusTestPair ms = new MutationStatusTestPair(2,
        DetectionStatus.KILLED, "foo");
    this.testee.report(mi, ms);
    final SafeDataInputStream is = resultToStream();
    assertThat(is.readByte()).isEqualTo(Id.REPORT);
    assertThat(is.read(MutationIdentifier.class)).isEqualTo(mi);
    assertThat(is.read(MutationStatusTestPair.class)).isEqualTo(ms);
  }

  private SafeDataInputStream resultToStream() {
    return new SafeDataInputStream(
        new ByteArrayInputStream(this.os.toByteArray()));
  }

  @Test
  public void shouldSendExitCode() {
    this.testee.done(ExitCode.TIMEOUT);
    final SafeDataInputStream is = resultToStream();
    assertThat(is.readByte()).isEqualTo(Id.DONE);
    assertThat(is.readInt()).isEqualTo(ExitCode.TIMEOUT.getCode());
  }

}
