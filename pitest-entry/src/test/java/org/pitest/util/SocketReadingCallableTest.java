package org.pitest.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyByte;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.net.UnixDomainSocketAddress;
import java.nio.channels.Channels;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SocketReadingCallableTest {

  private SocketReadingCallable             testee;

  private ServerSocketChannel               socket;

  @Mock
  private Consumer<SafeDataOutputStream> sendDataSideEffect;

  @Mock
  private ReceiveStrategy                   receiveStrategy;

  private ByteArrayOutputStream             o;

  @Before
  public void setUp() throws IOException {
    MockitoAnnotations.openMocks(this);
    this.socket = new SocketFinder().getNextAvailableServerSocket();
    this.testee = new SocketReadingCallable(this.socket,
        this.sendDataSideEffect, this.receiveStrategy);
    this.o = new ByteArrayOutputStream();
  }

  @Test
  public void shouldReportTheExitCodeSentByTheMinionProcess() throws Exception {
    sendExitCode(ExitCode.TIMEOUT);
    assertThat(callWithClient()).isEqualTo(ExitCode.TIMEOUT);
  }

  @Test
  public void shouldSendInitialDataToMinion() throws Exception {
    sendExitCode(ExitCode.TIMEOUT);
    callWithClient();
    verify(this.sendDataSideEffect).accept(any(SafeDataOutputStream.class));
  }

  @Test
  public void shouldPassNotPassDoneCommandToReceiver() throws Exception {
    sendExitCode(ExitCode.TIMEOUT);
    callWithClient();
    verify(this.receiveStrategy, never()).apply(anyByte(),
        any(SafeDataInputStream.class));
  }

  @Test
  public void shouldPassCommandsToReceiver() throws Exception {
    final SafeDataOutputStream dos = new SafeDataOutputStream(this.o);
    dos.writeByte(Id.DESCRIBE);
    dos.writeByte(Id.DONE);
    dos.writeInt(ExitCode.OK.getCode());
    callWithClient();
    verify(this.receiveStrategy, times(1)).apply(anyByte(),
        any(SafeDataInputStream.class));
  }

  @Test
  public void shouldRemoveSocketFileWhenDone() throws Exception {
    final Path path = SocketFinder.pathOf(this.socket);
    sendExitCode(ExitCode.OK);
    callWithClient();
    assertThat(Files.exists(path)).isFalse();
  }

  private ExitCode callWithClient() throws Exception {
    final Path path = SocketFinder.pathOf(this.socket);
    final Thread client = new Thread(() -> {
      try (SocketChannel c = SocketChannel
          .open(UnixDomainSocketAddress.of(path))) {
        Channels.newOutputStream(c).write(this.o.toByteArray());
        // keep the channel open until the server has sent its data and closed
        Channels.newInputStream(c).readAllBytes();
      } catch (final IOException e) {
        throw new RuntimeException(e);
      }
    });
    client.start();
    try {
      return this.testee.call();
    } finally {
      client.join();
    }
  }

  private void sendExitCode(final ExitCode code) throws IOException {
    final SafeDataOutputStream dos = new SafeDataOutputStream(this.o);
    dos.writeByte(Id.DONE);
    dos.writeInt(code.getCode());
  }

}
